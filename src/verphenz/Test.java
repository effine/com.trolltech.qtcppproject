package verphenz;

import com.trolltech.qtcppproject.utils.TargetPlatformType;

/**
 * @author arno
 * @version 2013-6-6 обнГ2:45:48
 */

public class Test {

	public static void main(String[] args) {

		TargetPlatformType n = new TargetPlatformType();

		for (String str : n.wPlatform) {
			System.out.println(str.toString());
		}

		System.out.println("-------------------------");
		System.out.println(System.getProperty("os.name"));
	}
}
