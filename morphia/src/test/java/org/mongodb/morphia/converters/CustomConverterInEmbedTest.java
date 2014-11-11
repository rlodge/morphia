package org.mongodb.morphia.converters;


import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.junit.Assert;
import org.junit.Test;
import org.mongodb.morphia.TestBase;
import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.mapping.MappedField;
import org.mongodb.morphia.testutil.TestEntity;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Uwe Schaefer
 */
@SuppressWarnings("rawtypes")
public class CustomConverterInEmbedTest extends TestBase {

    public static class E1 extends TestEntity {
        private final List<Foo> foo = new LinkedList<Foo>();
    }

    public static class E2 extends TestEntity {
        private final Map<String, Foo> foo = new HashMap<String, Foo>();
    }

    // unknown type to convert
    public static class Foo {
        private String string;

        Foo() {
        }

        public Foo(final String string) {
            this.string = string;
        }

        @Override
        public String toString() {
            return string;
        }
    }

    public static class FooConverter extends TypeConverter implements SimpleValueConverter {

        private boolean done;

        public FooConverter() {
            super(Foo.class);
        }

        @Override
        public Object decode(final Class targetClass, final Object fromDBObject, final MappedField optionalExtraInfo) {
            return new Foo((String) fromDBObject);
        }

        @Override
        public Object encode(final Object value, final MappedField optionalExtraInfo) {
            done = true;
            return value.toString();
        }

        public boolean didConversion() {
            return done;
        }
    }

    //FIXME issue 101

    @Test
    public void testConversionInList() throws Exception {
        final FooConverter fc = new FooConverter();
        getMorphia().getMapper().getConverters().addConverter(fc);
        final E1 e = new E1();
        e.foo.add(new Foo("bar"));
        getDs().save(e);
        Assert.assertTrue(fc.didConversion());
    }

    @Test
    public void testConversionInMap() throws Exception {
        final FooConverter fc = new FooConverter();
        getMorphia().getMapper().getConverters().addConverter(fc);
        E2 e = new E2();
        e.foo.put("bar", new Foo("bar"));
        getDs().save(e);

        Assert.assertTrue(fc.didConversion());

        e = getDs().find(E2.class).get();
        Assert.assertNotNull(e.foo);
        Assert.assertFalse(e.foo.isEmpty());
        Assert.assertTrue(e.foo.containsKey("bar"));
        Assert.assertEquals("bar", e.foo.get("bar").string);
    }

    /**
     * A type that contains a complex custom type, represented as an object.
     *
     * @author Christian Trimble
     */
    @Converters(ComplexFooConverter.class)
    public static class ComplexBar extends TestEntity {
        private ComplexFoo foo;

        public ComplexFoo getFoo() {
            return foo;
        }

        public void setFoo(final ComplexFoo foo) {
            this.foo = foo;
        }
    }

    public static class ChildClass {
        private String field1;

        public ChildClass() {
        }

        public ChildClass(final String field1) {
            this.field1 = field1;
        }

        public String getField1() {
            return field1;
        }

        public void setField1(final String field1) {
            this.field1 = field1;
        }
    }

    /**
     * A type that contains a complex custom type, represented as an object.
     *
     * @author Christian Trimble
     */
    @Converters(ChildClassConverter.class)
    public static class ComplexBarChild extends ComplexBar {
        private ChildClass bar;

        public ComplexBarChild() {
        }

        public ComplexBarChild(final ChildClass bar) {
            this.bar = bar;
        }

        public ChildClass getBar() {
            return bar;
        }

        public void setBar(final ChildClass bar) {
            this.bar = bar;
        }
    }

    /**
     * A type that contains a complex custom type, represented as an array.
     *
     * @author Christian Trimble
     */
    @Converters(ComplexArrayFooConverter.class)
    public static class ArrayBar extends TestEntity {
        private ArrayFoo foo;
    }

    /**
     * A complex embedded type, represented as an object
     *
     * @author Christian Trimble
     */
    public static class ComplexFoo {
        private String first;
        private String second;

        ComplexFoo() {
        }

        public ComplexFoo(final String first, final String second) {
            this.first = first;
            this.second = second;
        }

        String first() {
            return first;
        }

        String second() {
            return second;
        }
    }

    /**
     * A complex embedded type, represented as an array
     *
     * @author Christian Trimble
     */
    public static class ArrayFoo {
        private String first;
        private String second;

        ArrayFoo() {
        }

        public ArrayFoo(final String first, final String second) {
            this.first = first;
            this.second = second;
        }

        String first() {
            return first;
        }

        String second() {
            return second;
        }
    }

    /**
     * A converter that does not implement SimpleValueConverter and converts ComplexFoo into an object type.
     *
     * @author Christian Trimble
     */
    public static class ComplexFooConverter extends TypeConverter {
        public ComplexFooConverter() {
            super(ComplexFoo.class);
        }

        @Override
        public Object decode(final Class targetClass, final Object fromDBObject, final MappedField optionalExtraInfo) {
            DBObject dbObject = (DBObject) fromDBObject;
            return new ComplexFoo((String) dbObject.get("first"), (String) dbObject.get("second"));
        }

        @Override
        public Object encode(final Object value, final MappedField optionalExtraInfo) {
            ComplexFoo complex = (ComplexFoo) value;
            BasicDBObject dbObject = new BasicDBObject();
            dbObject.put("first", complex.first());
            dbObject.put("second", complex.second());
            return dbObject;
        }
    }

    /**
     * A converter that does not implement SimpleValueConverter and converts ArrayFoo into an array type.
     *
     * @author Christian Trimble
     */
    public static class ComplexArrayFooConverter extends TypeConverter {
        public ComplexArrayFooConverter() {
            super(ArrayFoo.class);
        }

        @Override
        public Object decode(final Class targetClass, final Object fromDBObject, final MappedField optionalExtraInfo) {
            BasicDBList dbObject = (BasicDBList) fromDBObject;
            return new ArrayFoo((String) dbObject.get(1), (String) dbObject.get(2));
        }

        @Override
        public Object encode(final Object value, final MappedField optionalExtraInfo) {
            ArrayFoo complex = (ArrayFoo) value;
            BasicDBList dbObject = new BasicDBList();
            dbObject.put(1, complex.first());
            dbObject.put(2, complex.second());
            return dbObject;
        }
    }

    /**
     * A converter that does not implement SimpleValueConverter and converts ComplexFoo into an object type.
     *
     * @author Christian Trimble
     */
    public static class ChildClassConverter extends TypeConverter {
        public ChildClassConverter() {
            super(ChildClass.class);
        }

        @Override
        public Object decode(final Class targetClass, final Object fromDBObject, final MappedField optionalExtraInfo) {
            DBObject dbObject = (DBObject) fromDBObject;
            ChildClass childClass = new ChildClass();
            childClass.field1 = (String) dbObject.get("x");
            return childClass;
        }

        @Override
        public Object encode(final Object value, final MappedField optionalExtraInfo) {
            ChildClass child = (ChildClass) value;
            BasicDBObject dbObject = new BasicDBObject();
            dbObject.put("x", child.field1);
            return dbObject;
        }
    }

    @Test
    public void testEmbeddedComplexType() throws Exception {
        ComplexBar bar = new ComplexBar();
        bar.foo = new ComplexFoo("firstValue", "secondValue");
        getDs().save(bar);

        ComplexBar fromDb = getDs().get(ComplexBar.class, bar.getId());
        assertThat("bar is not null", fromDb, notNullValue());
        assertThat("foo is not null", fromDb.foo, notNullValue());
        assertThat("foo has the correct first value", fromDb.foo.first(), equalTo("firstValue"));
        assertThat("foo has the correct second value", fromDb.foo.second(), equalTo("secondValue"));
    }

    /**
     * Tests that converters are gathered from all classes in the hierarchy, instead of only the topmost.
     *
     * @throws Exception
     */
    @Test
    public void testChildClassConverters() throws Exception {
        ComplexBarChild bar = new ComplexBarChild();
        bar.setFoo(new ComplexFoo("firstValue", "secondValue"));
        bar.setBar(new ChildClass("qzr"));
        getDs().save(bar);

        ComplexBarChild fromDb = getDs().find(ComplexBarChild.class).disableValidation().field("bar.x").equal("qzr").get();
        assertThat("bar is not null", fromDb, notNullValue());
        assertThat("foo is not null", fromDb.getFoo(), notNullValue());
        assertThat("foo has the correct first value", fromDb.getFoo().first(), equalTo("firstValue"));
        assertThat("foo has the correct second value", fromDb.getFoo().second(), equalTo("secondValue"));
        assertThat("bar has the correct value", fromDb.getBar().getField1(), equalTo("qzr"));
    }

    @Test
    public void testEmbeddedComplexArrayType() throws Exception {
        ArrayBar bar = new ArrayBar();
        bar.foo = new ArrayFoo("firstValue", "secondValue");
        getDs().save(bar);

        ArrayBar fromDb = getDs().get(ArrayBar.class, bar.getId());
        assertThat("bar is not null", fromDb, notNullValue());
        assertThat("foo is not null", fromDb.foo, notNullValue());
        assertThat("foo has the correct first value", fromDb.foo.first(), equalTo("firstValue"));
        assertThat("foo has the correct second value", fromDb.foo.second(), equalTo("secondValue"));
    }

}
