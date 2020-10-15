/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.mapping.model;

import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.lang.Nullable;

/**
 * @author Christoph Strobl
 * @since 2020/10
 */
public class AccessorFunctionPropertyAccessorFactory implements PersistentPropertyAccessorFactory {

	private static final AccessorFunctionPropertyAccessorFactory INSTANCE = new AccessorFunctionPropertyAccessorFactory();

	public static AccessorFunctionPropertyAccessorFactory instance() {
		return INSTANCE;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.mapping.model.PersistentPropertyAccessorFactory#getPropertyAccessor(org.springframework.data.mapping.PersistentEntity, java.lang.Object)
	 */
	@Override
	public <T> PersistentPropertyAccessor<T> getPropertyAccessor(PersistentEntity<?, ?> entity, T bean) {

		System.out.println("Obtaining static property acessor for entity " + entity.getName());
		return new AccessorFunctionPropertyAccessor<>((AccessorFunctionAware<T>) entity.getTypeInformation(), bean);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.mapping.model.PersistentPropertyAccessorFactory#isSupported(org.springframework.data.mapping.PersistentEntity)
	 */
	@Override
	public boolean isSupported(PersistentEntity<?, ?> entity) {

		boolean isStaticTypedEntity = entity.getTypeInformation() instanceof AccessorFunctionAware;
		System.out.println(entity.getName() + " isStaticTypedEntity: " + isStaticTypedEntity);
		return isStaticTypedEntity;
	}

	/**
	 * @author Christoph Strobl
	 * @param <T>
	 */
	static class AccessorFunctionPropertyAccessor<T> implements PersistentPropertyAccessor<T> {

		T bean;
		AccessorFunctionAware<T> accessorFunctionAware;

		public AccessorFunctionPropertyAccessor(AccessorFunctionAware<T> accessorFunctionAware, T bean) {
			this.bean = bean;
			this.accessorFunctionAware = accessorFunctionAware;
		}

		@Override
		public void setProperty(PersistentProperty<?> property, @Nullable Object value) {

			if (!accessorFunctionAware.hasSetFunctionFor(property.getName())) {
				return;
			}

			this.bean = accessorFunctionAware.getSetFunctionFor(property.getName()).apply(bean, value);
			System.out.println(
					"setting value " + value + " via setter function for " + property.getName() + " resulting in " + bean);
		}

		@Nullable
		@Override
		public Object getProperty(PersistentProperty<?> property) {

			if (!accessorFunctionAware.hasGetFunctionFor(property.getName())) {
				return null;
			}

			Object value = accessorFunctionAware.getGetFunctionFor(property.getName()).apply(bean);
			System.out.println("obtaining value " + value + " from getter function for " + property.getName());
			return value;
		}

		@Override
		public T getBean() {
			return this.bean;
		}
	}
}
