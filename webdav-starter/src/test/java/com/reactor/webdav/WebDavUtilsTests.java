package com.reactor.webdav;

import com.reactor.webdav.dto.LockInfo;
import com.reactor.webdav.dto.ParseUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;

@SpringBootTest
class WebDavUtilsTests {

  String rootFolder = "/home/alekcei/webdav";
  String propfindBody = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
		  " <D:propfind xmlns:D=\"DAV:\">\n" +
		  "  <D:prop>\n" +
		  "<D:creationdate/>\n" +
		  "<D:displayname/>\n" +
		  "<D:getcontentlength/>\n" +
		  "<D:getcontenttype/>\n" +
		  "<D:getetag/>\n" +
		  "<D:getlastmodified/>\n" +
		  "<D:resourcetype/>\n" +
		  "  </D:prop>\n" +
		  " </D:propfind>";



  String rq2 = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
		  " <D:propfind xmlns:D=\"DAV:\">\n" +
		  "  <D:prop>\n" +
		  "<D:creationdate/>\n" +
		  "<D:displayname/>\n" +
		  "<D:getcontentlength/>\n" +
		  "<D:getcontenttype/>\n" +
		  "<D:getetag/>\n" +
		  "<D:getlastmodified/>\n" +
		  "<D:resourcetype/>\n" +
		  "  </D:prop>\n" +
		  " </D:propfind>";
	@Autowired
	private PropfindToRsService toRq;



	@Test
	void fileInfo() {
		var lockInfo = ParseUtils.parseLockInfoRequest("<D:lockinfo xmlns:D=\"DAV:\">\n" +
				"\t<D:lockscope>\n" +
				"\t\t<D:exclusive/>\n" +
				"\t</D:lockscope>\n" +
				"\t<D:locktype>\n" +
				"\t\t<D:write/>\n" +
				"\t</D:locktype>\n" +
				"\t<D:owner>\n" +
				"\t\t<D:href>alekseisosnovskikh</D:href>\n" +
				"\t</D:owner>\n" +
				"</D:lockinfo>");

		assert lockInfo.getScope() == LockInfo.Scope.exclusive;
	}

	@Test
	@Disabled
	void contextLoads() {
//		var r = toRq.propfind("/", "1", propfindBody);
//		var listFiles =  toRq.propfindResponse(rootFolder, r);
//		toRq.toResponseStreem(listFiles);
	}


	@SneakyThrows
	@Test
	void getIconFile() {
		// Icon icon = FileSystemView.getFileSystemView().getSystemIcon(new File("/home/alekcei/Downloads/c2b_sbp_2024-01-05.pdf"));

		JFileChooser chooser = new JFileChooser();
		File f = new File("/home/alekcei/Downloads/c2b_sbp_2024-01-05.pdf");
		Icon icon = chooser.getIcon(f);
		BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
		icon.paintIcon(null, image.getGraphics(), 0, 0);
		File outputfile = new File("/home/alekcei/example.jpg");
		ImageIO.write(image, "jpg", outputfile);

	}

}
