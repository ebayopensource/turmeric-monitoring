/*******************************************************************************
 * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/
package org.ebayopensource.turmeric.monitoring.util;

import java.lang.reflect.Constructor;

/**
 * The Class ReflectionUtil.
 */
public class ReflectionUtil {
	private ReflectionUtil() {
		// no instances
	}

	/**
	 * Load class.
	 *
	 * @param <T> the generic type
	 * @param className the class name
	 * @param targetType the target type
	 * @param cl the cl
	 * @return the class
	 * @throws Exception the exception
	 */
	public static <T> Class<T> loadClass(String className, Class<T> targetType, ClassLoader cl)
		throws Exception
	{
		return loadClass(className, targetType, false, cl);
	}

	/**
	 * Load class.
	 *
	 * @param <T> the generic type
	 * @param className the class name
	 * @param targetType the target type
	 * @param ignoreMissingClass the ignore missing class
	 * @param cl the cl
	 * @return the class
	 * @throws Exception the exception
	 */
	public static <T> Class<T> loadClass(String className, Class<T> targetType,
		boolean ignoreMissingClass, ClassLoader cl)
		throws Exception
	{
		String targetTypeName;
		if (targetType != null) {
			targetTypeName = targetType.getName();
		} else {
			targetTypeName = "(unspecified assignment type)";
		}

		Class clazz;
		try {
			clazz = Class.forName(className, true, cl);
		} catch (NoClassDefFoundError err) {
			throw new Exception(err);
			//throw new Exception(SystemErrorTypes.SVC_FACTORY_INST_NOT_FOUND,
			//		new Object[] {targetTypeName, className}, err);
		} catch (ClassNotFoundException e) {
			if (ignoreMissingClass) {
				return null;
			}
			throw new Exception(e);
//			throw new ServiceException(SystemErrorTypes.SVC_FACTORY_INST_NOT_FOUND,
//				new Object[] {targetTypeName, className}, e);
		}

		if (targetType != null && !targetType.isAssignableFrom(clazz)) {
			throw new Exception("instance cannot cast - targetTypeName: " + targetTypeName
					+ ", className: " + className);
//			throw new ServiceException(SystemErrorTypes.SVC_FACTORY_INST_CANNOT_CAST,
//				new Object[] {targetTypeName, className});
		}

		@SuppressWarnings("unchecked")
		Class<T> result = clazz;

		return result;
	}

	/**
	 * Creates the instance.
	 *
	 * @param <T> the generic type
	 * @param className the class name
	 * @param targetType the target type
	 * @param cl the cl
	 * @return the t
	 * @throws Exception the exception
	 */
	public static <T> T createInstance(String className, Class<T> targetType, ClassLoader cl)
		throws Exception
	{
		return createInstance(className, targetType, cl, null, null);
	}

	/**
	 * Creates the instance.
	 *
	 * @param <T> the generic type
	 * @param className the class name
	 * @param targetType the target type
	 * @param cl the cl
	 * @param paramTypes the param types
	 * @param params the params
	 * @return the t
	 * @throws Exception the exception
	 */
	public static <T> T createInstance(String className, Class<T> targetType,
		ClassLoader cl, Class[] paramTypes, Object[] params)
		throws Exception
	{
		Class<T> clazz = loadClass(className, targetType, cl);

		return createInstance(clazz, paramTypes, params);
	}

	/**
	 * Creates the instance.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @return the t
	 * @throws Exception the exception
	 */
	public static <T> T createInstance(Class<T> clazz)
		throws Exception
	{
		return createInstance(clazz, null, null);
	}

	/**
	 * Creates the instance.
	 *
	 * @param <T> the generic type
	 * @param clazz the clazz
	 * @param paramTypes the param types
	 * @param params the params
	 * @return the t
	 * @throws Exception the exception
	 */
	public static <T> T createInstance(Class<T> clazz, Class[] paramTypes, Object[] params)
		throws Exception
	{
		Object result;
		try {
			if (paramTypes != null) {
				Constructor con = clazz.getConstructor(paramTypes);
				result = con.newInstance(params);
			} else {
				result = clazz.newInstance();
			}
		} catch (IllegalAccessException e) {
			throw new Exception(e);
//			throw new ServiceException(SystemErrorTypes.SVC_FACTORY_INST_ILLEGAL_ACCESS,
//				new Object[] {clazz.getName()}, e);
		} catch (Exception e) {
			throw new Exception(e);
//			throw new ServiceException(SystemErrorTypes.SVC_FACTORY_INST_EXCEPTION,
//				new Object[] {clazz.getName()}, e);
		}

		// type cast cannot be done, but we've checked isAssignableFrom
		@SuppressWarnings("unchecked")
		T result2 = (T)result;

		return result2;
	}

}
