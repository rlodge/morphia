package org.mongodb.morphia.utils;


import org.junit.Assert;
import org.junit.Test;
import org.mongodb.morphia.TestBase;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.Indexes;
import org.mongodb.morphia.mapping.Mapper;

import java.io.Serializable;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 * @author Scott Hernandez
 */
public class ReflectionUtilsTest extends TestBase {

    /**
     * Test method for {@link ReflectionUtils#implementsInterface(Class, Class)} .
     */
    @Test
    public void testImplementsInterface() {
        Assert.assertTrue(ReflectionUtils.implementsInterface(ArrayList.class, List.class));
        Assert.assertTrue(ReflectionUtils.implementsInterface(ArrayList.class, Collection.class));
        Assert.assertFalse(ReflectionUtils.implementsInterface(Set.class, List.class));
    }

    @Test
    public void testInheritedClassAnnotations() {
        final List<Indexes> annotations = ReflectionUtils.getAnnotations(Foobie.class, Indexes.class);
        Assert.assertEquals(2, annotations.size());
        Assert.assertTrue(ReflectionUtils.getAnnotation(Foobie.class, Indexes.class) != null);

        Assert.assertTrue("Base".equals(ReflectionUtils.getClassEntityAnnotation(Foo.class).value()));

        Assert.assertTrue("Sub".equals(ReflectionUtils.getClassEntityAnnotation(Foobie.class).value()));

        Assert.assertEquals(Mapper.IGNORED_FIELDNAME, ReflectionUtils.getClassEntityAnnotation(Fooble.class).value());
    }

    @Test
    public void testGetParameterizedClassInheritance() throws Exception {
        // Work before fix...
        Assert.assertEquals(Object.class, ReflectionUtils.getParameterizedClass(Set.class));
        Assert.assertEquals(Author.class, ReflectionUtils.getParameterizedClass(Book.class.getDeclaredField("authorsSet")));

        // Works now...
        Assert.assertEquals(Author.class, ReflectionUtils.getParameterizedClass(Book.class.getDeclaredField("authors")));

        Assert.assertEquals(Author.class, ReflectionUtils.getParameterizedClass(Authors.class));

        Assert.assertEquals(Author.class, ReflectionUtils.getParameterizedClass(WritingTeam.class));
    }

    /**
     * Tests that in a class hierarchy of arbitrary depth, we can get the correct declared field type
     *
     * @throws Exception
     */
    @Test
    public void testGenericFieldTypeResolution() throws Exception {
        Assert.assertEquals(
            Integer.class,
            ReflectionUtils.getTypeArgument(Sub.class,
                (TypeVariable) Super1.class.getDeclaredField("field").getGenericType()
            )
        );
    }

    @Entity("Base")
    @Indexes(@Index("id"))
    private static class Foo {
        @Id
        private int id;
    }

    @Entity("Sub")
    @Indexes(@Index("test"))
    private static class Foobie extends Foo {
        private String test;
    }

    @Entity
    private static class Fooble extends Foobie {
    }

    private static class Author {
    }

    private static class Authors extends HashSet<Author> {
        // Can contain utils methods
    }

    private static class WritingTeam extends Authors {
    }

    private static class Book {
        private Authors authors;

        private Set<Author> authorsSet;
    }

    private static class Super1<T extends Object> {
        private T field;

        public T getField() {
            return field;
        }

        public void setField(final T field) {
            this.field = field;
        }
    }

    private static class Super2<T extends Serializable> extends Super1<T> {
    }

    private static class Super3<T extends Number> extends Super2<T> {
    }

    private static class Sub extends Super3<Integer> {
    }

}
