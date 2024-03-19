/*
 */

package oolite.starter.generic;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A JPanel that only scrolls vertically.
 * See https://stackoverflow.com/questions/36163364/jscrollpane-with-fixed-width/36164503#36164503
 * See https://tips4java.wordpress.com/2009/12/20/scrollable-panel/
 * 
 * @author hiran
 */
public class ScrollablePanel extends JPanel implements Scrollable, SwingConstants {
    private static final Logger log = LogManager.getLogger();
    
    private static final String SCROLLABLE_PANEL_INVAILD_ORIENTATION = "Invalid orientation: ";
    
    public enum ScrollableSizeHint {
            NONE,
            FIT,
            STRETCH;
    }

    public enum IncrementType {
            PERCENT,
            PIXELS;
    }

    private ScrollableSizeHint scrollableHeight = ScrollableSizeHint.NONE;
    private ScrollableSizeHint scrollableWidth  = ScrollableSizeHint.NONE;

    private transient IncrementInfo horizontalBlock;
    private transient IncrementInfo horizontalUnit;
    private transient IncrementInfo verticalBlock;
    private transient IncrementInfo verticalUnit;

    /**
     *  Default constructor that uses a FlowLayout.
     */
    public ScrollablePanel() {
        this( new FlowLayout() );
    }

    /**
     *  Constuctor for specifying the LayoutManager of the panel.
     *
     *  @param layout the LayountManger for the panel
     */
    public ScrollablePanel(LayoutManager layout) {
        super( layout );
        log.debug("ScrollablePanel(...)");

        IncrementInfo block = new IncrementInfo(IncrementType.PERCENT, 100);
        IncrementInfo unit = new IncrementInfo(IncrementType.PERCENT, 10);

        setScrollableBlockIncrement(HORIZONTAL, block);
        setScrollableBlockIncrement(VERTICAL, block);
        setScrollableUnitIncrement(HORIZONTAL, unit);
        setScrollableUnitIncrement(VERTICAL, unit);
    }

    /**
     *  Get the height ScrollableSizeHint enum.
     *
     *  @return the ScrollableSizeHint enum for the height
     */
    public ScrollableSizeHint getScrollableHeight() {
        log.debug("getScrollableHeight()");
        return scrollableHeight;
    }

    /**
     *  Set the ScrollableSizeHint enum for the height. The enum is used to
     *  determine the boolean value that is returned by the
     *  getScrollableTracksViewportHeight() method. The valid values are:
     *
     *  ScrollableSizeHint.NONE - return "false", which causes the height
     *      of the panel to be used when laying out the children
     *  ScrollableSizeHint.FIT - return "true", which causes the height of
     *      the viewport to be used when laying out the children
     *  ScrollableSizeHint.STRETCH - return "true" when the viewport height
     *      is greater than the height of the panel, "false" otherwise.
     *
     *  @param scrollableHeight as represented by the ScrollableSizeHint enum.
     */
    public void setScrollableHeight(ScrollableSizeHint scrollableHeight) {
        log.debug("setScrollableHeight(...)");
        this.scrollableHeight = scrollableHeight;
        revalidate();
    }

    /**
     * Get the width ScrollableSizeHint enum.
     *
     * @return the ScrollableSizeHint enum for the width
     */
    public ScrollableSizeHint getScrollableWidth() {
        log.debug("getScrollableWidth()");
        return scrollableWidth;
    }

    /**
     *  Set the ScrollableSizeHint enum for the width. The enum is used to
     *  determine the boolean value that is returned by the
     *  getScrollableTracksViewportWidth() method. The valid values are:
     *
     *  ScrollableSizeHint.NONE - return "false", which causes the width
     *      of the panel to be used when laying out the children
     *  ScrollableSizeHint.FIT - return "true", which causes the width of
     *      the viewport to be used when laying out the children
     *  ScrollableSizeHint.STRETCH - return "true" when the viewport width
     *      is greater than the width of the panel, "false" otherwise.
     *
     *  @param scrollableWidth as represented by the ScrollableSizeHint enum.
     */
    public void setScrollableWidth(ScrollableSizeHint scrollableWidth) {
        log.debug("setScrollableWidth(...)");
        this.scrollableWidth = scrollableWidth;
        revalidate();
    }

    /**
     *  Get the block IncrementInfo for the specified orientation.
     *
     *  @return the block IncrementInfo for the specified orientation
     */
    public IncrementInfo getScrollableBlockIncrement(int orientation) {
        log.debug("getScrollableBlockIncrement(...)");
        return orientation == SwingConstants.HORIZONTAL ? horizontalBlock : verticalBlock;
    }

    /**
     *  Specify the information needed to do block scrolling.
     *
     *  @param orientation  specify the scrolling orientation. Must be either:
     *      SwingContants.HORIZONTAL or SwingContants.VERTICAL.
     *  @paran type  specify how the amount parameter in the calculation of
     *      the scrollable amount. Valid values are:
     *      IncrementType.PERCENT - treat the amount as a % of the viewport size
     *      IncrementType.PIXEL - treat the amount as the scrollable amount
     *  @param amount  a value used with the IncrementType to determine the
     *      scrollable amount
     */
    public void setScrollableBlockIncrement(int orientation, IncrementType type, int amount) {
        log.debug("setScrollableBlockIncrement(...)");
        IncrementInfo info = new IncrementInfo(type, amount);
        setScrollableBlockIncrement(orientation, info);
    }

    /**
     *  Specify the information needed to do block scrolling.
     *
     *  @param orientation  specify the scrolling orientation. Must be either:
     *      SwingContants.HORIZONTAL or SwingContants.VERTICAL.
     *  @param info  An IncrementInfo object containing information of how to
     *      calculate the scrollable amount.
     */
    public void setScrollableBlockIncrement(int orientation, IncrementInfo info) {
        log.debug("setScrollableBlockIncrement(...)");
        switch(orientation) {
            case SwingConstants.HORIZONTAL:
                horizontalBlock = info;
                break;
            case SwingConstants.VERTICAL:
                verticalBlock = info;
                break;
            default:
                throw new IllegalArgumentException(SCROLLABLE_PANEL_INVAILD_ORIENTATION + orientation);
        }
    }

    /**
     *  Get the unit IncrementInfo for the specified orientation.
     *
     *  @return the unit IncrementInfo for the specified orientation
     */
    public IncrementInfo getScrollableUnitIncrement(int orientation) {
        log.debug("getScrollableUnitIncrement(...)");
        return orientation == SwingConstants.HORIZONTAL ? horizontalUnit : verticalUnit;
    }

    /**
     *  Specify the information needed to do unit scrolling.
     *
     *  @param orientation  specify the scrolling orientation. Must be either:
     *      SwingContants.HORIZONTAL or SwingContants.VERTICAL.
     *  @paran type  specify how the amount parameter in the calculation of
     *               the scrollable amount. Valid values are:
     *               IncrementType.PERCENT - treat the amount as a % of the viewport size
     *               IncrementType.PIXEL - treat the amount as the scrollable amount
     *  @param amount  a value used with the IncrementType to determine the
     *                 scrollable amount
     */
    public void setScrollableUnitIncrement(int orientation, IncrementType type, int amount) {
        log.debug("setScrollableUnitIncrement(...)");
        IncrementInfo info = new IncrementInfo(type, amount);
        setScrollableUnitIncrement(orientation, info);
    }

    /**
     *  Specify the information needed to do unit scrolling.
     *
     *  @param orientation  specify the scrolling orientation. Must be either:
     *      SwingContants.HORIZONTAL or SwingContants.VERTICAL.
     *  @param info  An IncrementInfo object containing information of how to
     *               calculate the scrollable amount.
     */
    public void setScrollableUnitIncrement(int orientation, IncrementInfo info) {
        log.debug("setScrollableUnitIncrement(...)");
        switch(orientation) {
            case SwingConstants.HORIZONTAL:
                horizontalUnit = info;
                break;
            case SwingConstants.VERTICAL:
                verticalUnit = info;
                break;
            default:
                throw new IllegalArgumentException(SCROLLABLE_PANEL_INVAILD_ORIENTATION + orientation);
        }
    }

//  Implement Scrollable interface

    /**
     * Returns the preferred size of the viewport for a view component. 
     * For example, the preferred size of a JList component is the size 
     * required to accommodate all of the cells in its list. 
     * However, the value of preferredScrollableViewportSize is the size 
     * required for JList.getVisibleRowCount rows. 
     * A component without any properties that would affect the viewport 
     * size should just return getPreferredSize here.
     * 
     * @return the preferredSize of a JViewport whose view is this Scrollable
     * @see JComponent.getPreferredSize() 
     */
    public Dimension getPreferredScrollableViewportSize() {
        log.debug("getPreferredScrollableViewportSize()");
        return getPreferredSize();
    }

    /**
     * Components that display logical rows or columns should compute the 
     * scroll increment that will completely expose one new row or column, 
     * depending on the value of orientation. 
     * Ideally, components should handle a partially exposed row or column by 
     * returning the distance required to completely expose the item.
     *
     * Scrolling containers, like JScrollPane, will use this method each time 
     * the user requests a unit scroll.
     *
     * @param visibleRect The view area visible within the viewport
     * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
     * @param direction Less than zero to scroll up/left, greater than zero for down/right.
     * @see The "unit" increment for scrolling in the specified direction. 
     *      This value should always be positive.
     * @see JScrollBar.setUnitIncrement(int)
     */
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        log.debug("getScrollableUnitIncrement(...)");
        switch(orientation) {
            case SwingConstants.HORIZONTAL:
                return getScrollableIncrement(horizontalUnit, visibleRect.width);
            case SwingConstants.VERTICAL:
                return getScrollableIncrement(verticalUnit, visibleRect.height);
            default:
                throw new IllegalArgumentException(SCROLLABLE_PANEL_INVAILD_ORIENTATION + orientation);
        }
    }

    /**
     * Components that display logical rows or columns should compute the 
     * scroll increment that will completely expose one block of rows or 
     * columns, depending on the value of orientation.
     * Scrolling containers, like JScrollPane, will use this method each time 
     * the user requests a block scroll.
     * 
     * @param visibleRect The view area visible within the viewport
     * @param orientation Either SwingConstants.VERTICAL or SwingConstants.HORIZONTAL.
     * @param direction Less than zero to scroll up/left, greater than zero for down/right.
     * @return The "block" increment for scrolling in the specified direction. This value should always be positive.
     * @see JScrollBar.setBlockIncrement(int)
     */
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        log.debug("getScrollableBlockIncrement(...)");
        switch(orientation) {
            case SwingConstants.HORIZONTAL:
                return getScrollableIncrement(horizontalBlock, visibleRect.width);
            case SwingConstants.VERTICAL:
                return getScrollableIncrement(verticalBlock, visibleRect.height);
            default:
                throw new IllegalArgumentException(SCROLLABLE_PANEL_INVAILD_ORIENTATION + orientation);
        }
    }

    protected int getScrollableIncrement(IncrementInfo info, int distance) {
        log.debug("getScrollableIncrement(...)");
        if (info.getIncrement() == IncrementType.PIXELS)
            return info.getAmount();
        else
            return distance * info.getAmount() / 100;
    }

    /**
     * Return true if a viewport should always force the width of this 
     * Scrollable to match the width of the viewport. 
     * For example a normal text view that supported line wrapping would 
     * return true here, since it would be undesirable for wrapped lines 
     * to disappear beyond the right edge of the viewport. 
     * 
     * Note that returning true for a Scrollable whose ancestor is a 
     * JScrollPane effectively disables horizontal scrolling.
     * 
     * Scrolling containers, like JViewport, will use this method each time 
     * they are validated.
     * 
     * @return True if a viewport should force the Scrollables width to match its own. 
    */
    public boolean getScrollableTracksViewportWidth() {
        log.debug("getScrollableTracksViewportWidth(...)");
        if (scrollableWidth == ScrollableSizeHint.NONE)
            return false;

        if (scrollableWidth == ScrollableSizeHint.FIT)
            return true;

        //  STRETCH sizing, use the greater of the panel or viewport width

        if (getParent() instanceof JViewport jviewport) {
            return jviewport.getWidth() > getPreferredSize().width;
        }

        return false;
    }

    /**
     * Return true if a viewport should always force the height of this 
     * Scrollable to match the height of the viewport. 
     * For example a columnar text view that flowed text in left to right 
     * columns could effectively disable vertical scrolling by returning 
     * true here.
     * 
     * Scrolling containers, like JViewport, will use this method each time 
     * they are validated.
     * 
     * @return True if a viewport should force the Scrollables height to match its own. 
     */
    public boolean getScrollableTracksViewportHeight() {
        log.debug("getScrollableTracksViewportHeight(...)");
        if (scrollableHeight == ScrollableSizeHint.NONE)
            return false;

        if (scrollableHeight == ScrollableSizeHint.FIT)
            return true;

        //  STRETCH sizing, use the greater of the panel or viewport height

        if (getParent() instanceof JViewport jviewport) {
            return jviewport.getHeight() > getPreferredSize().height;
        }

        return false;
    }

    /**
     *  Helper class to hold the information required to calculate the scroll amount.
     */
    static class IncrementInfo {
        private IncrementType type;
        private int amount;

        public IncrementInfo(IncrementType type, int amount) {
            this.type = type;
            this.amount = amount;
        }

        public IncrementType getIncrement() {
            return type;
        }

        public int getAmount() {
            return amount;
        }

        public String toString() {
            return
                "ScrollablePanel[" +
                type + ", " +
                amount + "]";
        }
    }
}
