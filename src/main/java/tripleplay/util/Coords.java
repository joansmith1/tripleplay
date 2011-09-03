//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.Layer;

import pythagoras.f.IPoint;
import pythagoras.f.Point;

/**
 * Coordinate-related utility methods.
 */
public class Coords
{
    /**
     * Converts the supplied point from coordinates relative to the specified layer to screen
     * coordinates. The results are stored into {@code into}, which is returned for convenience.
     */
    public static Point layerToScreen (Layer layer, IPoint point, Point into) {
        return layerToParent(layer, null, point, into);
    }

    /**
     * Converts the supplied point from coordinates relative to the specified layer to screen
     * coordinates. The results are stored into {@code into}, which is returned for convenience.
     */
    public static Point layerToScreen (Layer layer, float x, float y, Point into) {
        return layerToScreen(layer, into.set(x, y), into);
    }

    /**
     * Converts the supplied point from coordinates relative to the specified child layer to
     * coordinates relative to the specified parent layer. The results are stored into {@code
     * into}, which is returned for convenience.
     */
    public static Point layerToParent (Layer layer, Layer parent, IPoint point, Point into) {
        into.set(point);
        while (layer != parent) {
            if (layer == null) {
                throw new IllegalArgumentException(
                    "Failed to find parent, perhaps you passed parent, layer instead of "+
                    "layer, parent?");
            }
            into.x -= layer.originX();
            into.y -= layer.originY();
            layer.transform().transform(into, into);
            layer = layer.parent();
        }
        return into;
    }

    /**
     * Converts the supplied point from coordinates relative to the specified child layer to
     * coordinates relative to the specified parent layer. The results are stored into {@code
     * into}, which is returned for convenience.
     */
    public static Point layerToParent (Layer layer, Layer parent, float x, float y, Point into) {
        return layerToParent(layer, parent, into.set(x, y), into);
    }

    /**
     * Converts the supplied point from screen coordinates to coordinates relative to the specified
     * layer. The results are stored into {@code into}, which is returned for convenience.
     */
    public static Point screenToLayer (Layer layer, IPoint point, Point into) {
        Layer parent = layer.parent();
        IPoint cur = (parent == null) ? point : screenToLayer(parent, point, into);
        into = layer.transform().inverseTransform(cur, into);
        into.x += layer.originX();
        into.y += layer.originY();
        return into;
    }

    /**
     * Converts the supplied point from screen coordinates to coordinates relative to the specified
     * layer. The results are stored into {@code into}, which is returned for convenience.
     */
    public static Point screenToLayer (Layer layer, float x, float y, Point into) {
        return screenToLayer(layer, into.set(x, y), into);
    }
}