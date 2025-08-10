package se.p950tes.subtitler;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import picocli.CommandLine;

@ExtendWith(MockitoExtension.class)
class SubtitlerApplicationTest {

	@Captor
	private ArgumentCaptor<String[]> captor;
	
	@Mock
	private CommandLine commandLine;
	
	@InjectMocks
	private SubtitlerApplication application;
	
	@Test
	void no_arguments() {
		application.execute();
		verifyArgs(new String[] {});
	}
	
	@Test
	void one_argument() {
		application.execute("-v");
		verifyArgs("-v");
	}
	@Test
	void two_arguments() {
		application.execute("-v", "--scrub");
		verifyArgs("-v", "--scrub");
	}
	private void verifyArgs(String... args) {
		verify(commandLine).execute(captor.capture());
		assertArrayEquals(args, captor.getValue());
	}
}
