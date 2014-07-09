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

package org.springframework.web.socket.config.annotation;

import java.util.Collections;

import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpSessionScope;

/**
 * Bean configuration for a "websocket" scope, bound to {@link SimpSessionScope}.
 *
 * @author Rossen Stoyanchev
 * @since 4.1
 */
public class WebSocketScopeConfiguration {

	@Bean
	public CustomScopeConfigurer webSocketScopeConfigurer() {
		CustomScopeConfigurer configurer = new CustomScopeConfigurer();
		configurer.setScopes(Collections.<String, Object>singletonMap("websocket", new SimpSessionScope()));
		return configurer;
	}

}
