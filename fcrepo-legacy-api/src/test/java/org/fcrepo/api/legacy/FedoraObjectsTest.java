
package org.fcrepo.api.legacy;

import static java.util.regex.Pattern.compile;
import static javax.ws.rs.core.MediaType.TEXT_XML;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class FedoraObjectsTest extends AbstractResourceTest {

    @Test
    public void testIngest() throws Exception {
        final HttpPost method = postObjMethod("FedoraObjectsTest1");
        final HttpResponse response = client.execute(method);
        assertEquals(201, response.getStatusLine().getStatusCode());
        final String content = EntityUtils.toString(response.getEntity());
        assertTrue("Response wasn't a PID", compile("[a-z]+").matcher(content)
                .find());
    }

    @Test
    public void testIngestWithNew() throws Exception {
        final HttpPost method = postObjMethod("new");
        final HttpResponse response = client.execute(method);
        assertEquals(201, response.getStatusLine().getStatusCode());
        final String content = EntityUtils.toString(response.getEntity());
        assertTrue("Response wasn't a PID", compile("[a-z]+").matcher(content)
                .find());
    }

    @Test
    public void testGetObjectInXML() throws Exception {
        client.execute(postObjMethod("FedoraObjectsTest2"));
        final HttpGet getObjMethod =
                new HttpGet(serverAddress + "objects/FedoraObjectsTest2");
        final HttpResponse response = client.execute(getObjMethod);
        assertEquals(200, response.getStatusLine().getStatusCode());
        final String content = EntityUtils.toString(response.getEntity());
        logger.debug("Retrieved object profile:\n" + content);
        assertTrue("Object had wrong PID!", compile(
                "pid=\"FedoraObjectsTest2\"").matcher(content).find());
    }

    @Test
    public void testDeleteObject() throws Exception {
        assertEquals(201, getStatus(postObjMethod("FedoraObjectsTest3")));
        assertEquals(204, getStatus(new HttpDelete(serverAddress +
                "objects/FedoraObjectsTest3")));
        assertEquals("Object wasn't really deleted!", 404,
                getStatus(new HttpGet(serverAddress +
                        "objects/FedoraObjectsTest3")));
    }

    @Test
    public void testModifyObject() throws Exception {
        final String label = "Muy especial!";
        assertEquals(201, getStatus(postObjMethod("FedoraObjectsTest4")));

        HttpPut modifyMethod =
                new HttpPut(serverAddress + "objects/FedoraObjectsTest4");
        modifyMethod
                .setEntity(new StringEntity(
                        "<objectProfile xmlns=\"http://www.fedora.info/definitions/1/0/access/\">" +
                                "<objLabel>" +
                                label +
                                "</objLabel>" +
                                "</objectProfile>"));
        modifyMethod.setHeader("Content-type", TEXT_XML);
        assertEquals(201, getStatus(modifyMethod));
        final HttpGet getMethod =
                new HttpGet(serverAddress + "objects/FedoraObjectsTest4");
        final HttpResponse response = client.execute(getMethod);
        final String content = EntityUtils.toString(response.getEntity());
        logger.debug("Retrieved object profile:\n" + content);
        assertTrue("Object wasn't updated!", compile(label).matcher(content)
                .find());
    }
}
