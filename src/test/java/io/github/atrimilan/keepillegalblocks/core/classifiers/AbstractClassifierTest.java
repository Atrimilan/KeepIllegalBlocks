package io.github.atrimilan.keepillegalblocks.core.classifiers;

import org.bukkit.block.data.BlockData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class AbstractClassifierTest {

    @Spy
    @InjectMocks
    private ReactiveClassifier classifier;

    public interface FirstInterfaceStub {}

    public interface SecondInterfaceStub {}

    @Test
    void shouldBlockDataHaveExpectedInterface() {
        BlockData mockBlock = mock(
                BlockData.class,
                Mockito.withSettings().extraInterfaces(FirstInterfaceStub.class)
        );
        assertTrue(classifier.hasAnyInterface(mockBlock, "FirstInterfaceStub"));
    }

    @Test
    void shouldBlockDataHaveAnyInterfaceFromExpectedList() {
        BlockData mockBlock = mock(
                BlockData.class,
                Mockito.withSettings().extraInterfaces(SecondInterfaceStub.class)
        );
        assertTrue(classifier.hasAnyInterface(mockBlock, "FirstInterfaceStub", "SecondInterfaceStub"));
    }

    @Test
    void shouldBlockDataNotHaveExpectedInterface() {
        BlockData mockBlock = mock(
                BlockData.class,
                Mockito.withSettings().extraInterfaces(FirstInterfaceStub.class)
        );
        assertFalse(classifier.hasAnyInterface(mockBlock, "SecondInterfaceStub"));
    }
}
