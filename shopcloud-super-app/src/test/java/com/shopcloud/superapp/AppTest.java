package com.shopcloud.superapp;

import com.shopcloud.superapp.zoom.ZoomHandler;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.Rectangle;
import junit.framework.TestCase;

import java.util.concurrent.CountDownLatch;

public class AppTest extends TestCase {

    static {
        try {
            // Initialize JavaFX Platform toolkit for testing JavaFX components
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void testZoomAccumulationAndDriftWithoutClamping() {
        Rectangle rect = new Rectangle(100, 100);
        ZoomHandler handler = new ZoomHandler(rect);
        handler.installOn(rect);

        // Verify initial state
        assertEquals(1.0, handler.getZoomLevel(), 1e-9);
        assertEquals(0.0, handler.getZoomRoot().getTranslateX(), 1e-9);
        assertEquals(0.0, handler.getZoomRoot().getTranslateY(), 1e-9);

        double sceneX = 50.0;
        double sceneY = 50.0;

        // Zoom in 10 times
        for (int i = 0; i < 10; i++) {
            ScrollEvent event = new ScrollEvent(
                    ScrollEvent.SCROLL, sceneX, sceneY, sceneX, sceneY,
                    false, false, false, false, false, false,
                    0, 40, 0, 40,
                    ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                    ScrollEvent.VerticalTextScrollUnits.NONE, 0,
                    0, null
            );
            rect.fireEvent(event);
        }

        // Check we scaled up to 1.1^10
        double expectedZoom = Math.pow(1.1, 10);
        assertEquals(expectedZoom, handler.getZoomLevel(), 1e-9);

        // Zoom out 10 times
        for (int i = 0; i < 10; i++) {
            ScrollEvent event = new ScrollEvent(
                    ScrollEvent.SCROLL, sceneX, sceneY, sceneX, sceneY,
                    false, false, false, false, false, false,
                    0, -40, 0, -40,
                    ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                    ScrollEvent.VerticalTextScrollUnits.NONE, 0,
                    0, null
            );
            rect.fireEvent(event);
        }

        // Verify we returned exactly to 1.0 zoom level and (0,0) translation with absolutely zero drift!
        assertEquals(1.0, handler.getZoomLevel(), 1e-9);
        assertEquals(0.0, handler.getZoomRoot().getTranslateX(), 1e-9);
        assertEquals(0.0, handler.getZoomRoot().getTranslateY(), 1e-9);
    }

    public void testZoomClampingAndImmediateResponse() {
        Rectangle rect = new Rectangle(100, 100);
        ZoomHandler handler = new ZoomHandler(rect);
        handler.installOn(rect);

        double sceneX = 50.0;
        double sceneY = 50.0;

        // Zoom in 30 times (reaches MAX_ZOOM of 4.0)
        for (int i = 0; i < 30; i++) {
            ScrollEvent event = new ScrollEvent(
                    ScrollEvent.SCROLL, sceneX, sceneY, sceneX, sceneY,
                    false, false, false, false, false, false,
                    0, 40, 0, 40,
                    ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                    ScrollEvent.VerticalTextScrollUnits.NONE, 0,
                    0, null
            );
            rect.fireEvent(event);
        }

        // Assert it is capped at MAX_ZOOM (4.0)
        assertEquals(4.0, handler.getZoomLevel(), 1e-9);

        // Zoom out exactly once - should respond immediately by scaling down
        ScrollEvent zoomOutEvent = new ScrollEvent(
                ScrollEvent.SCROLL, sceneX, sceneY, sceneX, sceneY,
                false, false, false, false, false, false,
                0, -40, 0, -40,
                ScrollEvent.HorizontalTextScrollUnits.NONE, 0,
                ScrollEvent.VerticalTextScrollUnits.NONE, 0,
                0, null
        );
        rect.fireEvent(zoomOutEvent);

        // Scale should decrease from 4.0 immediately to the 14th step (approx 3.797)
        double expectedZoomAfterOneZoomOut = Math.pow(1.1, 14);
        assertEquals(expectedZoomAfterOneZoomOut, handler.getZoomLevel(), 1e-9);
    }
}
