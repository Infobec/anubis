package anubis.utils;


import anubis.annotation.BeanProperties;
import anubis.generic.bean.SimpleGenericBean;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ReflectionUtils {

    public static List<Class> PRIMITIVE_TYPES = Arrays.asList(Byte.class, byte.class, Short.class, short.class, Integer.class, int.class, Long.class, long.class,
            Float.class, float.class, Double.class, double.class, Boolean.class, boolean.class, Character.class, String.class);

    public static String getLabelBean(Class<? extends SimpleGenericBean> classBean) {
        return getBeanProperties(classBean).label();
    }

    public static Field getFieldFromType(Class<?> clazz, Class<?> fieldFromType) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType() == fieldFromType) {
                return field;
            }
        }
        throw new RuntimeException("Não existe atributo do tipo " + fieldFromType.getName() + " na classe " + clazz.getName());
    }

    public static String getMessageGender(Class<? extends SimpleGenericBean> classBean) {
        return getBeanProperties(classBean).gender().getMessageKey();
    }

    public static BeanProperties getBeanProperties(Class<? extends SimpleGenericBean> classBean) {
        return classBean.getAnnotation(BeanProperties.class);
    }

    /**
     * Não valida campos da classe pai, valida Collections
     * Retorna o Field que estiver vazio, ou null caso não encontre nenhum Field vazio
     */
    public static Field anyEmptyValue(Object o) {
        Field anyEmptyValue = null;
        Field[] fields = o.getClass().getDeclaredFields();
        for(Field field : fields) {
            if(!isCollection(getValue(field, o)) && !isPrimitiveType(field)) {
                anyEmptyValue = anyEmptyValue(o);
                break;
            }

            if (isEmpty(field, o)) {
                anyEmptyValue = field;
                break;
            }
        }

        return anyEmptyValue;
    }

    public static boolean hasAnyEmptyValue(Object o) {
        return anyEmptyValue(o) != null;
    }

    private static boolean isCollection(Object obj) {
        if (obj instanceof Collection) {
            return true;
        }
        return false;
    }

    public static boolean isPrimitiveType(Field field) {
        return PRIMITIVE_TYPES.stream().anyMatch(primitiveClass -> primitiveClass.equals(field.getType()));
    }

    public static boolean isEmpty(Field field, Object o) {
        Object value = getValue(field, o);
        if (value == null) {
            return true;
        }
        if (value.getClass() == String.class) {
            return value.toString().isEmpty();
        }

        if (isCollection(value)) {
            Collection collection = (Collection) value;
            if (collection.isEmpty()) {
                return true;
            }
            return collection.stream().anyMatch(ReflectionUtils::hasAnyEmptyValue);
        }

        return false;
    }

    public static Object getValue(Field field, Object requerimento) {

        try {
            field.setAccessible(true);
            Object retorno = field.get(requerimento);
            field.setAccessible(false);
            return retorno;
        } catch (IllegalAccessException e) {
            field.setAccessible(false);
            throw new RuntimeException(e);
        }
    }
}
