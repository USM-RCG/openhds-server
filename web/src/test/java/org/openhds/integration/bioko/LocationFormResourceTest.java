package org.openhds.integration.bioko;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openhds.dao.service.GenericDao;
import org.openhds.domain.model.Location;
import org.openhds.integration.util.WebContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.server.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(loader = WebContextLoader.class, locations = { "/testContext.xml" })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class,
        DbUnitTestExecutionListener.class })
@DatabaseSetup(value = "/formResourceTestDb.xml", type = DatabaseOperation.REFRESH)
public class LocationFormResourceTest extends AbstractFormResourceTest {


    @Autowired
    private GenericDao genericDao;

    private MockHttpSession session;

    private MockMvc mockMvc;

    private static final String A_DATE = "2000-01-01T00:00:00-05:00";

    private static final String LOCATION_FORM_XML =
            "<locationForm>"
                    + "<processed_by_mirth>false</processed_by_mirth>"
                    + "<field_worker_ext_id>UNK</field_worker_ext_id>"
                    + "<location_ext_id>newLocation</location_ext_id>"
                    + "<collected_date_time>"
                    + A_DATE
                    + "</collected_date_time>"
                    + "<hierarchy_ext_id>IFB</hierarchy_ext_id>"
                    + "<location_name>newLocationName</location_name>"
                    + "<location_type>RUR</location_type>"
                    + "<community_name>newCommunityName</community_name>"
                    + "<map_area_name>newMapAreaName</map_area_name>"
                    + "<locality_name>newLocalityName</locality_name>"
                    + "<sector_name>newSectorName</sector_name>"
            + "</locationForm>";

    @Before
    public void setUp() throws Exception {
        mockMvc = buildMockMvc();
        session = getMockHttpSession("admin", "test", mockMvc);
    }

    @Test
    public void testPostLocationFormXml() throws Exception {
        mockMvc.perform(
                post("/locationForm").session(session).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML)
                        .body(LOCATION_FORM_XML.getBytes()))
                .andExpect(status().isCreated())
                .andExpect(content().mimeType(MediaType.APPLICATION_XML));

        verifyLocationCrud("newLocation");

    }

    public void verifyLocationCrud(String locationExtId) {

        Location persistedLocation = genericDao.findByProperty(Location.class, "extId", locationExtId);
        assertNotNull(persistedLocation);
        assertEquals("newLocationName", persistedLocation.getLocationName());
        assertEquals("RUR", persistedLocation.getLocationType());
        assertEquals("newCommunityName", persistedLocation.getCommunityName());
        assertEquals("newMapAreaName", persistedLocation.getMapAreaName());


    }


}
