package org.greenrobot.greendao.query;

import java.util.Date;
import java.util.List;
import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.SqlUtils;

public interface WhereCondition {

    public static abstract class AbstractCondition implements WhereCondition {
        protected final boolean hasSingleValue;
        protected final Object value;
        protected final Object[] values;

        public AbstractCondition() {
            this.hasSingleValue = false;
            this.value = null;
            this.values = null;
        }

        public AbstractCondition(Object value) {
            this.value = value;
            this.hasSingleValue = true;
            this.values = null;
        }

        public AbstractCondition(Object[] values) {
            this.value = null;
            this.hasSingleValue = false;
            this.values = values;
        }

        public void appendValuesTo(List<Object> valuesTarget) {
            if (this.hasSingleValue) {
                valuesTarget.add(this.value);
            } else if (this.values != null) {
                for (Object value : this.values) {
                    valuesTarget.add(value);
                }
            }
        }
    }

    public static class PropertyCondition extends AbstractCondition {
        public final String op;
        public final Property property;

        private static Object checkValueForType(Property property, Object value) {
            int i = 0;
            if (value != null && value.getClass().isArray()) {
                throw new DaoException("Illegal value: found array, but simple object required");
            } else if (property.type == Date.class) {
                if (value instanceof Date) {
                    return Long.valueOf(((Date) value).getTime());
                }
                if (value instanceof Long) {
                    return value;
                }
                throw new DaoException("Illegal date value: expected java.util.Date or Long for value " + value);
            } else if (property.type != Boolean.TYPE && property.type != Boolean.class) {
                return value;
            } else {
                if (value instanceof Boolean) {
                    if (((Boolean) value).booleanValue()) {
                        i = 1;
                    }
                    return Integer.valueOf(i);
                } else if (value instanceof Number) {
                    int intValue = ((Number) value).intValue();
                    if (intValue == 0 || intValue == 1) {
                        return value;
                    }
                    throw new DaoException("Illegal boolean value: numbers must be 0 or 1, but was " + value);
                } else if (!(value instanceof String)) {
                    return value;
                } else {
                    String stringValue = (String) value;
                    if ("TRUE".equalsIgnoreCase(stringValue)) {
                        return Integer.valueOf(1);
                    }
                    if ("FALSE".equalsIgnoreCase(stringValue)) {
                        return Integer.valueOf(0);
                    }
                    throw new DaoException("Illegal boolean value: Strings must be \"TRUE\" or \"FALSE\" (case insensitive), but was " + value);
                }
            }
        }

        private static Object[] checkValuesForType(Property property, Object[] values) {
            for (int i = 0; i < values.length; i++) {
                values[i] = checkValueForType(property, values[i]);
            }
            return values;
        }

        public PropertyCondition(Property property, String op) {
            this.property = property;
            this.op = op;
        }

        public PropertyCondition(Property property, String op, Object value) {
            super(checkValueForType(property, value));
            this.property = property;
            this.op = op;
        }

        public PropertyCondition(Property property, String op, Object[] values) {
            super(checkValuesForType(property, values));
            this.property = property;
            this.op = op;
        }

        public void appendTo(StringBuilder builder, String tableAlias) {
            SqlUtils.appendProperty(builder, tableAlias, this.property).append(this.op);
        }
    }

    public static class StringCondition extends AbstractCondition {
        protected final String string;

        public StringCondition(String string) {
            this.string = string;
        }

        public StringCondition(String string, Object value) {
            super(value);
            this.string = string;
        }

        public StringCondition(String string, Object... values) {
            super(values);
            this.string = string;
        }

        public void appendTo(StringBuilder builder, String tableAlias) {
            builder.append(this.string);
        }
    }

    void appendTo(StringBuilder stringBuilder, String str);

    void appendValuesTo(List<Object> list);
}
