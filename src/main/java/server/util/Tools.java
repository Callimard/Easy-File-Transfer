package server.util;

public class Tools {

	public static String toStringArray(String[] array) {
		StringBuilder builder = new StringBuilder("[");

		if (array != null) {

			for (int i = 0; i < array.length; i++) {
				if (i < array.length - 1) {
					builder.append(array[i] + ", ");
				} else {
					builder.append(array[i] + "]");
				}
			}

			return builder.toString();

		} else {
			return null;
		}
	}

}
