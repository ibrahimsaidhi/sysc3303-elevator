package sysc3303_elevator.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import sysc3303_elevator.Main;

class MainTest {

	@Test
	void test() {
		var items = new ArrayList<String>() {{
				add("a1");
				add("b1");
				add("c1");
				add("a2");
				add("b2");
				add("b3");
				add("c2");
				add("c3");
		}};

		var result = Main.GroupBy(items, item -> item.charAt(0));
		
		assertArrayEquals(result.get('a').toArray(), new String[] {"a1", "a2"});
		assertArrayEquals(result.get('b').toArray(), new String[] {"b1", "b2", "b3"});
		assertArrayEquals(result.get('c').toArray(), new String[] {"c1", "c2", "c3"});

		result.remove('a');
		result.remove('b');
		result.remove('c');
		assertEquals(0, result.size());
	}

}
