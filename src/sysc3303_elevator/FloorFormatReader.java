package sysc3303_elevator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * @author Quinn Parrott
 *
 */
public class FloorFormatReader {
	private BufferedReader inputStream;

	public FloorFormatReader(InputStream inputStream) {
		this.inputStream = new BufferedReader(new InputStreamReader(inputStream));
	}
	
	public FloorEvent next() throws IOException {
		String line = inputStream.readLine();
		if (line == null) {
			throw new IOException();
		}
		var attributes = line.split(" ");
		DateTimeFormatter parser = DateTimeFormatter.ofPattern("HH:mm:ss[.n]");
		LocalTime localTime = LocalTime.parse(attributes[0], parser);
		return new FloorEvent(
				localTime,
				Integer.parseInt(attributes[1]),
				attributes[2].toLowerCase().charAt(0) == 'u' ? Direction.Up : Direction.Down,
				Integer.parseInt(attributes[3])
		);
	}
	
	public ArrayList<FloorEvent> toList() {
		var list = new ArrayList<FloorEvent>();
		while (true) {
			try {
				list.add(this.next());
			} catch (IOException e) {
				break;
			}
		}
		return list;
	}
}
