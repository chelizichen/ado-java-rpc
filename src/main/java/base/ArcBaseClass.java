package base;

import config.ret;
import decorator.ArcInterFace;
import decorator.ArcMethod;
import decorator.ArcParams;
import decorator.ioc.Collect;
import decorator.ioc.Inject;
import decorator.ioc.Mapper;
import decorator.ioc.Service;
import decorator.orm.Entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author leemulus
 * @since 2023.2.16
 * 所有Rpc控制类基于此类
 */
public class ArcBaseClass {
    protected static Map<String, ArcBaseClass> ClazzMap = new HashMap();
    protected static Map<String, Class<?>> ParamsMap = new HashMap<>();
    protected static Map<String, Method> MethodsMap = new HashMap<>();
    protected static Map<String, Object> IocMap = new HashMap<>();


    public String InterFace;

    public ArcBaseClass() {
        boolean hasAnnotation = this.getClass().isAnnotationPresent(ArcInterFace.class);
        String interFaceName_Or_ClazzName = "";
        if (hasAnnotation) {
            ArcInterFace testAnnotation = this.getClass().getAnnotation(ArcInterFace.class);
            interFaceName_Or_ClazzName = testAnnotation.interFace();
        } else {
            interFaceName_Or_ClazzName = this.getClass().getSimpleName();
        }
        ArcBaseClass.ClazzMap.put(interFaceName_Or_ClazzName, this);
        this.InterFace = interFaceName_Or_ClazzName;
        // 单独设置参数
        this.SetParams();

        // 单独设置IOC
        this.SetIocService();
    }

    /**
     * @author chelizichen
     * @since 2023.2.16
     * use ioc to split code
     * use single
     */
    private void SetIocService() {
        final Field[] declaredFields = this.getClass().getDeclaredFields();
        for (Field field :
                declaredFields) {
            final boolean isInject = field.isAnnotationPresent(Inject.class);
            // 判断是否为 Dependency
            final boolean isCollect = field.getType().isAnnotationPresent(Collect.class);
            final boolean isAdoEntity = field.getType().isAnnotationPresent(Entity.class);
            final boolean isMapper = field.getType().isAnnotationPresent(Mapper.class);
            final boolean isService = field.getType().isAnnotationPresent(Service.class);

            if (isInject && (isCollect || isAdoEntity ||isMapper || isService)) {
                final String simpleName = field.getType().getSimpleName();
                final Object instance = ArcBaseClass.IocMap.get(simpleName);
                if (instance != null) {
                    field.setAccessible(true);
                    try {
                        field.set(this, instance);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        final Constructor<?> constructor = field.getType().getDeclaredConstructor();
                        try {
                            final Object ServiceInstance = constructor.newInstance();
                            field.setAccessible(true);
                            field.set(this, ServiceInstance);
                            final String serviceName = ServiceInstance.getClass().getSimpleName();
                            ArcBaseClass.IocMap.put(serviceName, ServiceInstance);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * @author leemulus
     * @since 2023.2.16
     * 用来设置公共参数类型
     * 加载阶段，将参数的类和类型别名加载到 ArcInterFace.ParamsMap 中
     * 运行阶段 通过注解中的 ApcParams.value 来获取对应类型 并加载
     */
    private void SetParams() {
        Method[] methods = this.getClass().getMethods();
        for (Method method : methods) {

            // 需要是 @ArcMethod 注解
            if (method.isAnnotationPresent(ArcMethod.class)) {

                // 创建 ObServer，将 Name + 方法名称带入;
                ArcBaseClass.MethodsMap.put(this.InterFace + method.getName(), method);

                final Class<?>[] parameterTypes = method.getParameterTypes();

                for (Class<?> parameterType : parameterTypes) {
                    final boolean annotationPresent = parameterType.isAnnotationPresent(ArcParams.class);
                    if (annotationPresent) {
                        final ArcParams annotation = parameterType.getAnnotation(ArcParams.class);
                        final String value = annotation.value();

                        // 当没有设置参数时
                        if (value.equals("")) {
                            final String simpleName = parameterType.getSimpleName();
                            ArcBaseClass.ParamsMap.put(simpleName, parameterType);
                        } else {
                            ArcBaseClass.ParamsMap.put(value, parameterType);
                        }
                    }
                }
            }
        }
    }

    /**
     * 传过来的一个args 参数
     * 遍历 args 将每个 args 进行类型转换 作为新的类型参数传递给 已存储的方法中实现调用
     */
    public ret invokeMethod(String interFace, String method, List<Object> args) {
        final Method getMethod = ArcBaseClass.MethodsMap.get(interFace + method);
        final Class<?>[] parameterTypes = getMethod.getParameterTypes();


        final ArrayList<Object> truthParams = new ArrayList<>(args.size() - 1); // 实参
        int index = 0;
        for (Class<?> parameterType : parameterTypes) {
            final boolean annotationPresent = parameterType.isAnnotationPresent(ArcParams.class);
            if (annotationPresent) {
                final List<String> list = (List) args.get(index);
                final ArcParams annotation = parameterType.getAnnotation(ArcParams.class);

                final String value = annotation.value();
                Class<?> cacheClass = null;
                if (value.equals("")) {
                    final String simpleName = parameterType.getSimpleName();
                    cacheClass = ArcBaseClass.ParamsMap.get(simpleName);
                } else {
                    cacheClass = ArcBaseClass.ParamsMap.get(value);
                }

                try {
                    final Class<?> aClass = Class.forName(cacheClass.getName());
                    try {
                        final Constructor<?> constructor = aClass.getDeclaredConstructor(List.class);
                        try {
//                            System.out.println("List is ->" + list);
                            final Object o = constructor.newInstance(list);
                            truthParams.add(index, o);
                        } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                truthParams.add(index, args.get(index));
            }
            index++;

        }
        final ArcBaseClass arcBaseClass = ArcBaseClass.ClazzMap.get(interFace);
        final int size = truthParams.size() - 1;
        return this.__invoke__(size, arcBaseClass, truthParams, getMethod);
    }

    private ret __invoke__(int size, ArcBaseClass arcBaseClass, List<Object> truthParams, Method getMethod) {
        ret retVal = null;
        try {
            if (size == 0) {
                retVal = (ret) getMethod.invoke(arcBaseClass, truthParams.get(0));
            } else if (size == 1) {
                retVal = (ret) getMethod.invoke(arcBaseClass, truthParams.get(0), truthParams.get(1));
            } else if (size == 2) {
                retVal = (ret) getMethod.invoke(arcBaseClass, truthParams.get(0), truthParams.get(1), truthParams.get(2));
            } else if (size == 3) {
                retVal = (ret) getMethod.invoke(arcBaseClass, truthParams.get(0), truthParams.get(1), truthParams.get(2), truthParams.get(3));
            } else if (size == 4) {
                retVal = (ret) getMethod.invoke(arcBaseClass, truthParams.get(0), truthParams.get(1), truthParams.get(2), truthParams.get(3), truthParams.get(4));
            } else if (size == 5) {
                retVal = (ret) getMethod.invoke(arcBaseClass, truthParams.get(0), truthParams.get(1), truthParams.get(2), truthParams.get(3), truthParams.get(4), truthParams.get(5));
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return retVal;
    }
}
