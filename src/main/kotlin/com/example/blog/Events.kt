package com.example.blog

import com.example.blog.share.api.models.CreateUser
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import org.springframework.context.event.EventListener
import org.springframework.core.ResolvableType
import org.springframework.core.ResolvableTypeProvider
import org.springframework.core.annotation.Order
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener


class UserRegistrationEvent(source: Any, val user: User) : ApplicationEvent(source)

@Service
class UserService(
    private val repository: UserRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val logger: Logger = LoggerFactory.getLogger(FlywayMigrator::class.java)

    fun CreateUser.toEntity(): User {

        return User(
            username = this.username,
            firstname = this.firstname,
            lastname = this.lastname,
            description = this.description
        )
    }

    fun getAllUsers() = repository.findAll()

    fun findUser(username: String) = repository.findByUsername(username)

    @Transactional
    fun createUser(createUser: CreateUser): User {
        val existingUser = repository.findByUsername(createUser.username)
        if (existingUser != null) {
            // TODO: In reality we would throw an error if the user exists already
            try {
                eventPublisher.publishEvent(UserRegistrationEvent(this, existingUser))
            } catch (ex: IllegalStateException){
                logger.error("Error in event handler!!!", ex);
            }
            return existingUser
        }

        val savedUser = repository.save(createUser.toEntity())

        // This will block if we don't use @Async and wait for all event handlers to finish.
        eventPublisher.publishEvent(UserRegistrationEvent(this, savedUser))
        return savedUser
    }
}

@Component
class UserRegistrationListener : ApplicationListener<UserRegistrationEvent> {
    private val logger: Logger = LoggerFactory.getLogger(FlywayMigrator::class.java)

    @Async
    // Note: We don't use the @Async notation in any repo. It first starts a thread which then calls our event handler
    //       so events can be processed at the same time.
    @Order(1)
    override fun onApplicationEvent(event: UserRegistrationEvent) {
        logger.info("UserRegistrationEvent: User [${event.user.username}] with ID ${event.user.id} created.");
    }
}

// I can't think of anything where generic events could be useful.
// Maybe if I wanted to do something with a generic collection, where it does not matter what the collection contains?
// I think for everything else I could just use base classes / interfaces.
class GenericUserRegistrationEvent<T>(source: Any, val user: T?) : ApplicationEvent(source), ResolvableTypeProvider {
    override fun getResolvableType(): ResolvableType? {
        return ResolvableType.forClassWithGenerics(javaClass, ResolvableType.forInstance(user))
    }
}

@Component
class UserRegistrationListenerGeneric {
    private val logger: Logger = LoggerFactory.getLogger(FlywayMigrator::class.java)
    @EventListener
    fun handleGenericUserRegistrationEvent(event: GenericUserRegistrationEvent<User>) {
        logger.info("handleGenericUserRegistrationEvent: User [${event.user?.username}] with ID ${event.user?.id} created.");
    }
}


@Component
class UserRegistrationListener2 {

    private val logger: Logger = LoggerFactory.getLogger(FlywayMigrator::class.java)

    // With this annotation any bean function can be an event listener (used in Optravis code base)
    @EventListener
    @Order(2)
    // Events can be ordered with the @Order annotation. The Order is respected for all events in the same class.
    // onApplicationEvent is executed out of order, because it is defined somewhere else.
    fun handleUserRegistrationEvent(event: UserRegistrationEvent) {
        logger.info("UserRegistrationEvent2: User [${event.user.username}] with ID ${event.user.id} created.");
        // This is caught, because it runs synchronously
//        throw IllegalStateException("Something is not right....")
    }

    @EventListener
    @Order(3)
    fun handleUserRegistrationEvent3(event: UserRegistrationEvent) {
        logger.info("UserRegistrationEvent3: User [${event.user.username}] with ID ${event.user.id} created.");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Order(4)
    fun handleUserRegistrationEventTransactional(event: UserRegistrationEvent) {
        logger.info("UserRegistrationEventTransactional: User [${event.user.username}] with ID ${event.user.id} created.");

        // This is not caught, because the event runs after the commit has already been completed
        // If we change the event to run before the commit, then the transaction fails, but the exception handler for
        // the publishEvent is not called, because the code already completed.
        // Like in asynchronous code, the exception is not propagated
        throw IllegalStateException("Something is not right, but transactional....")
    }
}