/**
 * This file is part of pwt.
 *
 * pwt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * pwt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with pwt.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.putnami.pwt.core.model.client.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import fr.putnami.pwt.core.editor.client.Path;
import fr.putnami.pwt.core.editor.client.Path.PathElement;
import fr.putnami.pwt.core.editor.client.validator.Validator;
import fr.putnami.pwt.core.model.client.model.Model;
import fr.putnami.pwt.core.model.client.model.ModelCollection;
import fr.putnami.pwt.core.model.client.model.PropertyDescription;

public final class ModelUtils {

	public static <A> PropertyDescription resolveProperty(Model<?> model, Path path) {
		if (model == null || path.size() == 0) {
			return null;
		}
		String firstElementName = path.get(0).getElementName();
		if (path.size() == 1) {
			return model.getProperty(firstElementName);
		}
		else if (Path.ROOT_PATH.equals(firstElementName)
				&& path.get(0).getIndexKey() != null) {
			return ModelUtils.resolveProperty(model, path.subPath(1));
		}
		else {
			PropertyDescription propertyDescription = model.getProperty(firstElementName);
			Model<Object> subModel = (Model<Object>) propertyDescription.getModel();
			return ModelUtils.resolveProperty(subModel, path.subPath(1));
		}
	}

	public static <A> Collection<Validator<A>> resolveValidators(Model<?> model, Path path) {
		PropertyDescription propertyDescription = resolveProperty(model, path);
		Collection<Validator<?>> validators = null;
		if (propertyDescription != null) {
			validators = propertyDescription.getValidators();
		}
		return validators == null ? Collections.EMPTY_LIST : validators;
	}

	public static <A, B> Model<A> resolveModel(Model<B> model, Path path) {
		PropertyDescription propertyDescription = resolveProperty(model, path);
		if (propertyDescription != null) {
			return (Model<A>) propertyDescription.getModel();
		}
		return (Model<A>) model;
	}

	public static <A, B> Class<A> resolveType(Model<B> model, Path path) {
		PropertyDescription propertyDescription = resolveProperty(model, path);
		if (propertyDescription != null) {
			return (Class<A>) propertyDescription.getClazz();
		}
		return null;
	}

	public static <A, B> A resolveValue(Object bean, Model<B> model, Path path) {
		if (model == null || path.size() == 0) {
			return (A) bean;
		}

		PathElement firstElement = path.get(0);
		String firstElementName = firstElement.getElementName();
		firstElementName = firstElementName == null ? Path.ROOT_PATH : firstElementName;

		Object value = bean;
		Model<?> leafModel = model;
		if (leafModel instanceof ModelCollection) {
			leafModel = ((ModelCollection) model).getLeafModel();
		}

		if (leafModel != null && !Path.ROOT_PATH.equals(firstElementName)) {
			value = leafModel.get(bean, firstElementName);
		}

		if (firstElement.getIndexKey() != null && value instanceof Collection) {
			Collection collection = (Collection) value;
			if (collection.size() > firstElement.getIndexKey()) {
				value = Iterables.get(collection, firstElement.getIndexKey());
			}
			else {
				value = null;
			}
		}

		if (path.size() == 1) {
			return (A) value;
		}
		else {
			if (!Path.ROOT_PATH.equals(firstElementName)) {
				leafModel = leafModel.getProperty(firstElementName).getModel();
			}
			return ModelUtils.resolveValue(value, leafModel, path.subPath(1));

		}
	}

	public static <A, B> void bindValue(Object bean, Model<A> model, Path path, B value) {
		if (model == null || path.size() == 0) {
			return;
		}

		PathElement firstElement = path.get(0);
		String firstElementName = firstElement.getElementName();
		Integer firstElementIndex = firstElement.getIndexKey();
		if (path.size() == 1 && firstElementIndex == null) {
			model.set(bean, firstElementName, value);
		}
		else if (path.size() == 1 && firstElementIndex != null) {
			Object o = model.get(bean, firstElementName);
			if (o == null) {
				o = model.newInstance();
				model.set(bean, firstElementName, o);
			}
			if (o instanceof List) {
				List list = (List) o;
				while (list.size() < firstElementIndex.intValue()) {
					list.add(null);
				}
				if (firstElementIndex.equals(list.size())) {
					list.add(value);
				}
				else {
					list.add(firstElementIndex, value);
				}
			}
		}
		else if (Path.ROOT_PATH.equals(firstElementName)
				&& path.size() == 2) {
			model.set(bean, path.get(1).getElementName(), value);
		}
		else if (firstElementIndex != null) {
			Object o = model.get(bean, firstElementName);
			if (o == null) {
				o = model.newInstance();
				model.set(bean, firstElementName, o);
			}
			if (o instanceof List) {
				List list = (List) o;
				int size = list.size();
				while (list.size() < firstElementIndex.intValue()) {
					list.add(null);
				}
				Object subBean = list.get(firstElementIndex);

				Model<Object> subModel = model.getLeafModel();
				if (subBean == null) {
					subBean = subModel.newInstance();
					list.add(firstElementIndex, subBean);
				}
				ModelUtils.bindValue(subBean, subModel, path.subPath(1), value);
			}

		}
		else {
			Model<Object> subModel = (Model<Object>) model.getProperty(firstElementName).getModel();
			Object subBean = model.get(bean, firstElementName);
			if (subBean == null) {
				subBean = subModel.newInstance();
				model.set(bean, firstElementName, subBean);
			}
			ModelUtils.bindValue(subBean, subModel, path.subPath(1), value);
		}
		return;
	}

	public static List<Class<?>> getTypeHierachy(Class<?> propertyType) {
		List<Class<?>> result = Lists.newArrayList();
		if (propertyType != null) {
			Class<?> parentClass = propertyType.getSuperclass();
			while (parentClass != null) {
				result.add(parentClass);
				parentClass = parentClass.getSuperclass();
			}
		}
		return result;
	}

	private ModelUtils() {
	}

	public static boolean isEnumType(Class propertyType) {
		for (Class<?> parentClass : ModelUtils.getTypeHierachy(propertyType)) {
			if (parentClass != null && Enum.class.equals(parentClass)) {
				return true;
			}
		}
		return false;
	}

}
