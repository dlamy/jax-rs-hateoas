/*
 * Copyright 2011 the original author or authors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jayway.jaxrs.hateoas.support;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.jayway.jaxrs.hateoas.HateoasLink;
import com.jayway.jaxrs.hateoas.HateoasVerbosity;
import com.jayway.jaxrs.hateoas.core.HateoasResponseBuilderImpl.FixedLinkProducer;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Mattias Hellborg Arthursson
 * @author Kalle Stenflo
 */
public class JavassistHateoasLinkInjectorTest {
	private final static Map<String, Object> EXPECTED_MAP = new HashMap<String, Object>();
	private HateoasLink linkMock;
	private JavassistHateoasLinkInjector tested;
	private FixedLinkProducer linkProducer;

	@Before
	public void prepareTestedInstance() {
		tested = new JavassistHateoasLinkInjector();
		linkMock = mock(HateoasLink.class);
		linkProducer = new FixedLinkProducer(linkMock);
		when(linkMock.toMap(HateoasVerbosity.MINIMUM)).thenReturn(EXPECTED_MAP);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void linksFieldIsInjectedAutomatically() {
		DummyEntity dummyEntity = new DummyEntity();
		dummyEntity.setId("someId");

		DummyEntity returnedEntity = (DummyEntity) tested.injectLinks(
				dummyEntity, linkProducer, HateoasVerbosity.MINIMUM);

		assertNotSame(dummyEntity, returnedEntity);
		assertEquals("someId", returnedEntity.getId());

		Collection<Map<String, Object>> links = (Collection<Map<String, Object>>) ReflectionUtils
				.getFieldValue(returnedEntity, "links");
		assertSame(EXPECTED_MAP, Iterables.getOnlyElement(links));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void linksFieldIsInjectedAutomaticallyInSublass() {
		DummySubClass dummyEntity = new DummySubClass("someId", 1L);

		DummySubClass returnedEntity = (DummySubClass) tested.injectLinks(dummyEntity, linkProducer, HateoasVerbosity.MINIMUM);

		assertNotSame(dummyEntity, returnedEntity);
		assertEquals("someId", returnedEntity.getId());
		assertEquals(1L, returnedEntity.getTime());

		Collection<Map<String, Object>> links = (Collection<Map<String, Object>>) ReflectionUtils
				.getFieldValue(returnedEntity, "links");
		assertSame(EXPECTED_MAP, Iterables.getOnlyElement(links));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void immutableDTOWithPublicConstructor() {
		ImmutablePublicDTO dto = new ImmutablePublicDTO(10L, "bar", true);

		ImmutablePublicDTO returnedEntity = (ImmutablePublicDTO) tested.injectLinks(dto, linkProducer, HateoasVerbosity.MINIMUM);



		assertNotSame(dto, returnedEntity);
		assertEquals(10L, returnedEntity.getFoo());
		assertEquals("bar", returnedEntity.getBar());

		Collection<Map<String, Object>> links = (Collection<Map<String, Object>>) ReflectionUtils
				.getFieldValue(returnedEntity, "links");
		assertSame(EXPECTED_MAP, Iterables.getOnlyElement(links));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void immutableDTOWithProtectedConstructor() {
		ImmutableProtectedDTO dto = new ImmutableProtectedDTO(10L, "bar", true);

		ImmutableProtectedDTO returnedEntity = (ImmutableProtectedDTO) tested.injectLinks(dto, linkProducer, HateoasVerbosity.MINIMUM);

		assertNotSame(dto, returnedEntity);
		assertEquals(10L, returnedEntity.getFoo());
		assertEquals("bar", returnedEntity.getBar());

		Collection<Map<String, Object>> links = (Collection<Map<String, Object>>) ReflectionUtils
				.getFieldValue(returnedEntity, "links");
		assertSame(EXPECTED_MAP, Iterables.getOnlyElement(links));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void immutableDTOPublicNoArgConstructor() {
		ImmutablePublicNoArgDTO dto = new ImmutablePublicNoArgDTO();
		dto.foo = 10L;
		dto.bar = "bar";
		dto.baz = true;

		ImmutablePublicNoArgDTO returnedEntity = (ImmutablePublicNoArgDTO) tested.injectLinks(dto, linkProducer, HateoasVerbosity.MINIMUM);

		assertNotSame(dto, returnedEntity);
		assertEquals(10L, returnedEntity.foo);
		assertEquals("bar", returnedEntity.bar);
		assertEquals(true, returnedEntity.baz);

		Collection<Map<String, Object>> links = (Collection<Map<String, Object>>) ReflectionUtils
				.getFieldValue(returnedEntity, "links");
		assertSame(EXPECTED_MAP, Iterables.getOnlyElement(links));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void immutableDTOWithPrivateConstructor() {
		try {
			tested.injectLinks(new ImmutablePrivateDTO(10L, "bar", true), linkProducer, HateoasVerbosity.MINIMUM);
			fail("should have thrown exception because it's a private exception");
		} catch (Exception e) {
			// no-op
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void immutableDTOWithPackageConstructor() {
		try {
			tested.injectLinks(new ImmutablePackageDTO(10L, "bar", true), linkProducer, HateoasVerbosity.MINIMUM);
			fail("should have thrown exception because it's a private exception");
		} catch (Exception e) {
			// no-op
		}
	}

	public static class DummyEntityNoDefaultConstructor {
		private String id;

		public DummyEntityNoDefaultConstructor(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}

	public static class DummyEntity {
		private String id;

		public DummyEntity() {
		}

		public DummyEntity(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}

	public static class DummySubClass extends DummyEntity {

		private long time;

		public DummySubClass() {
		}

		public DummySubClass(String id, long time) {
			super(id);
			this.time = time;
		}

		public long getTime() {
			return time;
		}
	}

	public static class ImmutablePublicDTO {
		private final long foo;
		private final String bar;
		private final boolean baz;

		public ImmutablePublicDTO(long foo, String bar, boolean baz) {
			this.foo = foo;
			this.bar = bar;
			this.baz = baz;
		}

		public long getFoo() {
			return foo;
		}

		public String getBar() {
			return bar;
		}
	}

	public static class ImmutablePrivateDTO {
		private final long foo;
		private final String bar;
		private final boolean baz;

		private ImmutablePrivateDTO(long foo, String bar, boolean baz) {
			this.foo = foo;
			this.bar = bar;
			this.baz = baz;
		}

		public long getFoo() {
			return foo;
		}

		public String getBar() {
			return bar;
		}
	}

	public static class ImmutableProtectedDTO {
		private final long foo;
		private final String bar;
		private final boolean baz;

		protected ImmutableProtectedDTO(long foo, String bar, boolean baz) {
			this.foo = foo;
			this.bar = bar;
			this.baz = baz;
		}

		public long getFoo() {
			return foo;
		}

		public String getBar() {
			return bar;
		}
	}

	public static class ImmutablePackageDTO {
		private final long foo;
		private final String bar;
		private final boolean baz;

		ImmutablePackageDTO(long foo, String bar, boolean baz) {
			this.foo = foo;
			this.bar = bar;
			this.baz = baz;
		}

		public long getFoo() {
			return foo;
		}

		public String getBar() {
			return bar;
		}
	}

	public static class ImmutablePublicNoArgDTO {
		public long foo;
		public String bar;
		private boolean baz;
	}


}
