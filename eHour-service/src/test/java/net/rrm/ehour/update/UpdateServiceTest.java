package net.rrm.ehour.update;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateServiceTest {
    @Mock
    private LatestVersionFetcher latestVersionFetcher;

    private UpdateService updateService;

    @Before
    public void setUp() throws Exception {
        updateService = new UpdateService("1.2", latestVersionFetcher);
    }

    @Test
    public void should_fetch_latest_version() throws Exception {
        updateService.fetchLatestVersion();

        verify(latestVersionFetcher).getLatestVersionNumber();
    }

    @Test
    public void should_say_version_is_latest_when_it_failed_to_retrieve() throws Exception {
        when(latestVersionFetcher.getLatestVersionNumber()).thenReturn(Optional.<String>absent());
        updateService.fetchLatestVersion();

        assertTrue(updateService.isLatestVersion());
    }

    @Test
    public void should_say_version_is_not_latest_when_version_dont_match() throws Exception {
        when(latestVersionFetcher.getLatestVersionNumber()).thenReturn(Optional.of("1.3"));
        updateService.fetchLatestVersion();

        assertFalse(updateService.isLatestVersion());
    }

    @Test
    public void should_say_version_is_latest_when_versions_match() throws Exception {
        when(latestVersionFetcher.getLatestVersionNumber()).thenReturn(Optional.of("1.2"));
        updateService.fetchLatestVersion();

        assertTrue(updateService.isLatestVersion());
    }
}
