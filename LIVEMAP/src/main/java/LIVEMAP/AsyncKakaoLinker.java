package  LIVEMAP;

import java.util.concurrent.*;

import java.util.concurrent.atomic.AtomicInteger;

/** 카카오 DB 연동을 백그라운드에서 처리 */
public final class AsyncKakaoLinker {

	private static final ThreadFactory TF = new ThreadFactory() {
		private final AtomicInteger n = new AtomicInteger(1);

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "kakao-linker-" + n.getAndIncrement());
			t.setDaemon(true);
			return t;
		}
	};

	private static final ExecutorService EXEC = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<>(1024), TF, new ThreadPoolExecutor.DiscardPolicy() // 큐 가득이면 버림
	);

	private AsyncKakaoLinker() {
	}



	/** 신규: 프로필 동봉해서 members 빈 칸까지 채움 */
	public static void submitEnsureLinkWithProfile(String providerUserId, KakaoProfileData data, String accessToken,
			String refreshToken) {
		if (providerUserId == null || providerUserId.isEmpty()) {
			System.err.println("[KAKAO][ASYNC] skip: providerUserId null/empty");
			return;
		}
		EXEC.execute(() -> {
			try {
				new MemberService().ensureKakaoLinkWithProfile(providerUserId, data, accessToken, refreshToken);
			} catch (Throwable t) {
				System.err.println("[KAKAO][ASYNC] ensureKakaoLinkWithProfile failed: " + t);
			}
		});
	}

	public static void shutdown() {
		EXEC.shutdown();
	}
}
