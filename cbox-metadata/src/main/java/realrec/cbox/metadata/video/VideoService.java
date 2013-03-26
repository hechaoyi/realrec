package realrec.cbox.metadata.video;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class VideoService {

	private SqlSessionFactory sessionFactory;

	private VideoService() {
		try (InputStream is = Resources
				.getResourceAsStream("mybatis-config.xml")) {
			sessionFactory = new SqlSessionFactoryBuilder().build(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String videoLength(String videoId) {
		try (SqlSession session = sessionFactory.openSession()) {
			return session.selectOne("Video.length", videoId);
		}
	}

	public VideoSetDetail videoSetDetail(String videoSetId) {
		try (SqlSession session = sessionFactory.openSession()) {
			return session.selectOne("Video.detail", videoSetId);
		}
	}

	private static VideoService instance = null;

	public static VideoService instance() {
		if (instance == null) {
			synchronized (VideoService.class) {
				if (instance == null)
					instance = new VideoService();
			}
		}
		return instance;
	}

}
