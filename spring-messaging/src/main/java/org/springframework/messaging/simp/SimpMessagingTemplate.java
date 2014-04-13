/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.messaging.simp;

import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.core.AbstractMessageSendingTemplate;
import org.springframework.messaging.core.MessagePostProcessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.support.NativeMessageHeaderAccessor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * An implementation of {@link org.springframework.messaging.simp.SimpMessageSendingOperations}.
 *
 * <p>Also provides methods for sending messages to a user. See
 * {@link org.springframework.messaging.simp.user.UserDestinationResolver UserDestinationResolver}
 * for more on user destinations.
 *
 * @author Rossen Stoyanchev
 * @since 4.0
 */
public class SimpMessagingTemplate extends AbstractMessageSendingTemplate<String>
		implements SimpMessageSendingOperations {

	private final MessageChannel messageChannel;

	private String userDestinationPrefix = "/user/";

	private volatile long sendTimeout = -1;


	/**
	 * Create a new {@link SimpMessagingTemplate} instance.
	 * @param messageChannel the message channel (must not be {@code null})
	 */
	public SimpMessagingTemplate(MessageChannel messageChannel) {
		Assert.notNull(messageChannel, "MessageChannel must not be null");
		this.messageChannel = messageChannel;
	}


	/**
	 * Configure the prefix to use for destinations targeting a specific user.
	 * <p>The default value is "/user/".
	 * @see org.springframework.messaging.simp.user.UserDestinationMessageHandler
	 */
	public void setUserDestinationPrefix(String prefix) {
		Assert.notNull(prefix, "UserDestinationPrefix must not be null");
		this.userDestinationPrefix = prefix;
	}

	/**
	 * @return the userDestinationPrefix
	 */
	public String getUserDestinationPrefix() {
		return this.userDestinationPrefix;
	}

	/**
	 * @return the messageChannel
	 */
	public MessageChannel getMessageChannel() {
		return this.messageChannel;
	}

	/**
	 * Specify the timeout value to use for send operations.
	 *
	 * @param sendTimeout the send timeout in milliseconds
	 */
	public void setSendTimeout(long sendTimeout) {
		this.sendTimeout = sendTimeout;
	}

	/**
	 * @return the sendTimeout
	 */
	public long getSendTimeout() {
		return this.sendTimeout;
	}


	/**
	 * If the headers of the given message already contain a
	 * {@link org.springframework.messaging.simp.SimpMessageHeaderAccessor#DESTINATION_HEADER
	 * SimpMessageHeaderAccessor#DESTINATION_HEADER} then the message is sent without
	 * further changes.
	 *
	 * <p>If a destination header is not already present ,the message is sent
	 * to the configured {@link #setDefaultDestination(Object) defaultDestination}
	 * or an exception an {@code IllegalStateException} is raised if that isn't
	 * configured.
	 *
	 * @param message the message to send, never {@code null}
	 */
	@Override
	public void send(Message<?> message) {
		Assert.notNull(message, "'message' is required");
		String destination = SimpMessageHeaderAccessor.getDestination(message.getHeaders());
		if (destination != null) {
			sendInternal(message);
			return;
		}
		doSend(getRequiredDefaultDestination(), message);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doSend(String destination, Message<?> message) {

		Assert.notNull(destination, "Destination must not be null");

		SimpMessageHeaderAccessor simpAccessor =
				MessageHeaderAccessor.getAccessor(message, SimpMessageHeaderAccessor.class);

		if (simpAccessor != null) {
			if (simpAccessor.isMutable()) {
				simpAccessor.setDestination(destination);
				simpAccessor.setMessageTypeIfNotSet(SimpMessageType.MESSAGE);
				simpAccessor.setImmutable();
				sendInternal(message);
				return;
			}
			else {
				// Try and keep the original accessor type
				simpAccessor = (SimpMessageHeaderAccessor) MessageHeaderAccessor.getMutableAccessor(message);
			}
		}
		else {
			simpAccessor = SimpMessageHeaderAccessor.wrap(message);
		}

		simpAccessor.setDestination(destination);
		simpAccessor.setMessageTypeIfNotSet(SimpMessageType.MESSAGE);
		message = MessageBuilder.createMessage(message.getPayload(), simpAccessor.getMessageHeaders());
		sendInternal(message);
	}

	private void sendInternal(Message<?> message) {

		String destination = SimpMessageHeaderAccessor.getDestination(message.getHeaders());
		Assert.notNull(destination);

		long timeout = this.sendTimeout;
		boolean sent = (timeout >= 0)
				? this.messageChannel.send(message, timeout)
				: this.messageChannel.send(message);

		if (!sent) {
			throw new MessageDeliveryException(message,
					"Failed to send message to destination '" + destination + "' within timeout: " + timeout);
		}
	}


	@Override
	public void convertAndSendToUser(String user, String destination, Object payload) throws MessagingException {
		this.convertAndSendToUser(user, destination, payload, (MessagePostProcessor) null);
	}

	@Override
	public void convertAndSendToUser(String user, String destination, Object payload,
			Map<String, Object> headers) throws MessagingException {

		this.convertAndSendToUser(user, destination, payload, headers, null);
	}

	@Override
	public void convertAndSendToUser(String user, String destination, Object payload,
			MessagePostProcessor postProcessor) throws MessagingException {

		this.convertAndSendToUser(user, destination, payload, null, postProcessor);
	}

	@Override
	public void convertAndSendToUser(String user, String destination, Object payload, Map<String, Object> headers,
			MessagePostProcessor postProcessor) throws MessagingException {

		Assert.notNull(user, "User must not be null");
		user = StringUtils.replace(user, "/", "%2F");
		super.convertAndSend(this.userDestinationPrefix + user + destination, payload, headers, postProcessor);
	}

	/**
	 * Creates a new map and puts the given headers under the key
	 * {@link org.springframework.messaging.support.NativeMessageHeaderAccessor#NATIVE_HEADERS NATIVE_HEADERS NATIVE_HEADERS NATIVE_HEADERS}.
	 * effectively treats the input header map as headers to be sent out to the
	 * destination.
	 *
	 * <p>However if the given headers already contain the key
	 * {@code NATIVE_HEADERS NATIVE_HEADERS} then the same headers instance is
	 * returned without changes.
	 *
	 * <p>Also if the given headers were prepared and obtained with
	 * {@link SimpMessageHeaderAccessor#getMessageHeaders()} then the same headers
	 * instance is also returned without changes.
	 */
	@Override
	protected Map<String, Object> processHeadersToSend(Map<String, Object> headers) {

		if (headers == null) {
			SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
			headerAccessor.setLeaveMutable(true);
			return headerAccessor.getMessageHeaders();
		}

		if (headers.containsKey(NativeMessageHeaderAccessor.NATIVE_HEADERS)) {
			return headers;
		}

		if (headers instanceof MessageHeaders) {
			SimpMessageHeaderAccessor accessor =
					MessageHeaderAccessor.getAccessor((MessageHeaders) headers, SimpMessageHeaderAccessor.class);
			if (accessor != null) {
				return headers;
			}
		}

		SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
		for (String key : headers.keySet()) {
			Object value = headers.get(key);
			headerAccessor.setNativeHeader(key, (value != null ? value.toString() : null));
		}
		return headerAccessor.getMessageHeaders();
	}

}
