package ch.hsr.osminabox.test.db.util;

import static org.junit.Assert.*;

import java.util.Set;
import java.util.TreeSet;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.hsr.osminabox.db.entities.Node;
import ch.hsr.osminabox.db.entities.OSMEntity;
import ch.hsr.osminabox.db.util.ValueConverter;

public class ValueConverterTest {
			
		private static ValueConverter valueConverter;
		private static Node node;
			
		@BeforeClass
		public static void initate() {
			valueConverter = new ValueConverter();
			node = new Node();
			
			node.setOsmId(11);
			node.attributes.put(OSMEntity.ATTRIBUTE_TIMESTAMP, "2009-07-31T07:26:05Z");
			node.attributes.put(Node.NODE_LATITUDE, "47.0713097");
			node.attributes.put(Node.NODE_LONGITUDE, "9.4865085");
			
			Set<String> created_by = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
			created_by.add("JOSM");
			node.tags.put("created_by", created_by);
			
			Set<String> name = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
			name.add("Kuhspitze");
			node.tags.put("name", name);
		}
		
		@Test
		public void convertConstantTest(){
			
			assertEquals("Test", valueConverter.convertValue("Test", node));
		}
		
		@Test
		public void convertTagTest(){
			assertEquals("Kuhspitze", valueConverter.convertValue("%tag_name%", node));
		}
		
		@Test
		public void convertAttributeTest(){
			assertEquals("11", valueConverter.convertValue("%attribute_id%", node));
			assertEquals("9.4865085", valueConverter.convertValue("%attribute_lon%", node));
		}
		
		@Test
		public void convertEmptyTest(){
			assertEquals("", valueConverter.convertValue("%tag_blablabla%", node));
		}
		
		@Test
		public void convertDuplicateKeyTest(){
			node.tags.get("name").add("Chuespitz");
			assertEquals("Chuespitz;Kuhspitze", valueConverter.convertValue("%tag_name%", node));
		}
		
		@Test
		public void convertTagsAllTest(){
			assertEquals("'\"created_by\"=>\"JOSM\" , \"name\"=>\"Chuespitz;Kuhspitze\"'", valueConverter.convertValue("%tags_all%", node));
		}
		
		@Test
		public void convertSpecialCharTest(){
			node.tags.get("created_by").clear();
			node.tags.get("created_by").add("Joram's \"C.O.O.L:-@%/()+~^=");
			assertEquals("Joram's \"C.O.O.L:-@%/()+~^=", valueConverter.convertValue("%tag_created_by%", node));
		}
	

}
