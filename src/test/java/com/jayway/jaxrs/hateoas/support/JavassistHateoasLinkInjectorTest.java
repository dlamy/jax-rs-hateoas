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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.jayway.jaxrs.hateoas.HateoasInjectException;
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
    private static Map<String, Object> REL_MAP = new HashMap<>();

    private HateoasLink linkMock;
    private HateoasLink linkMapMock;
    private JavassistHateoasLinkInjector arrayTested;
    private JavassistHateoasLinkInjector mapTested;
    private FixedLinkProducer arrayLinkProducer;
    private FixedLinkProducer mapLinkProducer;

    @Before
    public void prepareTestedInstance() {
        REL_MAP.put("rel", "test");
        REL_MAP.put("href", "href_value");
        arrayTested = new JavassistHateoasLinkInjector(false);
        mapTested = new JavassistHateoasLinkInjector(true);
        linkMock = mock(HateoasLink.class);
        linkMapMock = mock(HateoasLink.class);
        arrayLinkProducer = new FixedLinkProducer(linkMock);
        mapLinkProducer = new FixedLinkProducer(linkMapMock);

        when(linkMock.toMap(HateoasVerbosity.MINIMUM)).thenReturn(EXPECTED_MAP);
        when(linkMapMock.toMap(HateoasVerbosity.MINIMUM)).thenReturn(REL_MAP);
    }

	@SuppressWarnings("unchecked")
	@Test
	public void linksFieldIsInjectedAutomatically() {
		DummyEntity dummyEntity = new DummyEntity();
		dummyEntity.setId("someId");

        DummyEntity returnedEntity = (DummyEntity) arrayTested.injectLinks(
                dummyEntity, arrayLinkProducer, HateoasVerbosity.MINIMUM);

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

		DummySubClass returnedEntity = (DummySubClass) arrayTested.injectLinks(dummyEntity, arrayLinkProducer, HateoasVerbosity.MINIMUM);

		assertNotSame(dummyEntity, returnedEntity);
		assertEquals("someId", returnedEntity.getId());
		assertEquals(1L, returnedEntity.getTime());

		Collection<Map<String, Object>> links = (Collection<Map<String, Object>>) ReflectionUtils
				.getFieldValue(returnedEntity, "links");
		assertSame(EXPECTED_MAP, Iterables.getOnlyElement(links));
	}

    @SuppressWarnings("unchecked")
    @Test
    public void linksMapFieldIsInjectedAutomatically() {
        DummyMapEntity dummyEntity = new DummyMapEntity();
        dummyEntity.setId("someId");

        DummyMapEntity returnedEntity = (DummyMapEntity) mapTested.injectLinks(
                dummyEntity, mapLinkProducer, HateoasVerbosity.MINIMUM);

        assertNotSame(dummyEntity, returnedEntity);
        assertEquals("someId", returnedEntity.getId());

        Map<String, Map<String, Object>> links = (Map<String, Map<String, Object>>) ReflectionUtils
                .getFieldValue(returnedEntity, "links");

        assertTrue(links.size() == 1);
        assertEquals(links.keySet().iterator().next(), "test");
        assertEquals(links.values().iterator().next().size(), 1);
        assertEquals(links.values().iterator().next().keySet().iterator().next(), "href");
        assertEquals(links.values().iterator().next().values().iterator().next(), "href_value");
    }

	@SuppressWarnings("unchecked")
	@Test
	public void linksMapFieldIsInjectedAutomaticallyInSublass() {
		DummyMapSubClass dummyEntity = new DummyMapSubClass("someId", 1L);

		DummyMapSubClass returnedEntity = (DummyMapSubClass) mapTested.injectLinks(dummyEntity, mapLinkProducer, HateoasVerbosity.MINIMUM);

		assertNotSame(dummyEntity, returnedEntity);
		assertEquals("someId", returnedEntity.getId());
		assertEquals(1L, returnedEntity.getTime());

		Map<String, Map<String, Object>> links = (Map<String, Map<String, Object>>) ReflectionUtils
				.getFieldValue(returnedEntity, "links");
		assertTrue(links.size() == 1);
		assertEquals(links.keySet().iterator().next(), "test");
		assertEquals(links.values().iterator().next().size(), 1);
		assertEquals(links.values().iterator().next().keySet().iterator().next(), "href");
		assertEquals(links.values().iterator().next().values().iterator().next(), "href_value");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void immutableDTOWithPublicConstructor() {
		ImmutablePublicDTO dto = new ImmutablePublicDTO(10L, "bar", true);

		ImmutablePublicDTO returnedEntity = (ImmutablePublicDTO) arrayTested.injectLinks(dto, arrayLinkProducer, HateoasVerbosity.MINIMUM);

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

		ImmutableProtectedDTO returnedEntity = (ImmutableProtectedDTO) arrayTested.injectLinks(dto, arrayLinkProducer, HateoasVerbosity.MINIMUM);

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

		ImmutablePublicNoArgDTO returnedEntity = (ImmutablePublicNoArgDTO) arrayTested.injectLinks(dto, arrayLinkProducer, HateoasVerbosity.MINIMUM);

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
	public void mutablePublicNoArgConstructorDefined() {
		MutableWithNoArgDefinedDTO dto = new MutableWithNoArgDefinedDTO();
		dto.rows = ImmutableList.of("foo", "bar", "baz");

		MutableWithNoArgDefinedDTO returnedEntity = (MutableWithNoArgDefinedDTO) arrayTested.injectLinks(dto, arrayLinkProducer, HateoasVerbosity.MINIMUM);

		assertNotSame(dto, returnedEntity);

		Collection<Map<String, Object>> links = (Collection<Map<String, Object>>) ReflectionUtils
				.getFieldValue(returnedEntity, "links");
		assertSame(EXPECTED_MAP, Iterables.getOnlyElement(links));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void immutableDTOWithPrivateConstructor() {
		try {
			arrayTested.injectLinks(new ImmutablePrivateDTO(10L, "bar", true), arrayLinkProducer, HateoasVerbosity.MINIMUM);
			fail("should have thrown exception because it's a private exception");
		} catch (Exception e) {
			// no-op
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void immutableDTOWithPackageConstructor() {
		try {
			arrayTested.injectLinks(new ImmutablePackageDTO(10L, "bar", true), arrayLinkProducer, HateoasVerbosity.MINIMUM);
			fail("should have thrown exception because it's a private exception");
		} catch (Exception e) {
			// no-op
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void npeInConstructor() {
		try {
			arrayTested.injectLinks(new MutableWithNoArgDefinedNPEDTO(), arrayLinkProducer, HateoasVerbosity.MINIMUM);
			fail("should have thrown a NPE because null gets passed into the constructor");
		} catch (Exception e) {
			System.out.println();
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

	public static class DummyMapEntity {
        private String id;

        public DummyMapEntity() {
        }

        public DummyMapEntity(String id) {
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

	public static class DummyMapSubClass extends DummyMapEntity {

		private long time;

		public DummyMapSubClass() {
		}

		public DummyMapSubClass(String id, long time) {
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

	public class MutableWithNoArgDefinedDTO {
		public Collection<String> rows;

		public MutableWithNoArgDefinedDTO() {}
		public MutableWithNoArgDefinedDTO(List<String> rows) {
			if (rows != null) {
				this.rows = new ArrayList<>(rows);
			}
		}
	}

	public class MutableWithNoArgDefinedNPEDTO {
		public Collection<String> rows;

		public MutableWithNoArgDefinedNPEDTO() {}
		public MutableWithNoArgDefinedNPEDTO(List<String> rows) {
			this.rows = new ArrayList<>(rows);
		}
	}

}
