/*****************************************************************************
 **
 ** @author Lee Neuse (coder@humbleprogrammer.net)
 ** @since 1.0
 **
 **	---------------------------- [License] ----------------------------------
 **	This work is licensed under the Creative Commons Attribution-NonCommercial-
 **	ShareAlike 3.0 Unported License. To view a copy of this license, visit
 **				http://creativecommons.org/licenses/by-nc-sa/3.0/
 **	or send a letter to Creative Commons, 444 Castro Street Suite 900, Mountain
 **	View, California, 94041, USA.
 **	--------------------- [Disclaimer of Warranty] --------------------------
 **	There is no warranty for the program, to the extent permitted by applicable
 **	law.  Except when otherwise stated in writing the copyright holders and/or
 **	other parties provide the program “as is” without warranty of any kind,
 **	either expressed or implied, including, but not limited to, the implied
 **	warranties of merchantability and fitness for a particular purpose.  The
 **	entire risk as to the quality and performance of the program is with you.
 **	Should the program prove defective, you assume the cost of all necessary
 **	servicing, repair or correction.
 **	-------------------- [Limitation of Liability] --------------------------
 **	In no event unless required by applicable law or agreed to in writing will
 **	any copyright holder, or any other party who modifies and/or conveys the
 **	program as permitted above, be liable to you for damages, including any
 **	general, special, incidental or consequential damages arising out of the
 **	use or inability to use the program (including but not limited to loss of
 **	data or data being rendered inaccurate or losses sustained by you or third
 **	parties or a failure of the program to operate with any other programs),
 **	even if such holder or other party has been advised of the possibility of
 **	such damages.
 **
 ******************************************************************************/
package net.humbleprogrammer.e4.gui.views;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.humbleprogrammer.e4.Command;
import net.humbleprogrammer.e4.ResourceManager;
import net.humbleprogrammer.e4.gui.themes.ITheme;
import net.humbleprogrammer.e4.gui.themes.ThemeManager;
import net.humbleprogrammer.humble.DBC;
import net.humbleprogrammer.humble.GfxUtil;
import net.humbleprogrammer.maxx.Square;

public class BoardView extends net.humbleprogrammer.e4.gui.controls.EnhancedPanel
	{

	//  -----------------------------------------------------------------------
	//	CONSTANTS
	//	-----------------------------------------------------------------------

	/** Default square size, measured in pixels. */
	private static final int DEFAULT_SQ_DIM = 48;
	/** Minimum square size, measured in pixels. */
	private static final int MIN_SQ_DIM     = 12;

	//  -----------------------------------------------------------------------
	//	STATIC DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Logger */
	private static final Logger s_log = LoggerFactory.getLogger( BoardView.class );

	//  -----------------------------------------------------------------------
	//	DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Rectangle between board labels and squares. */
	private final Rectangle   _rInner = new Rectangle();
	/** Rectangle around outside edge of board labels. */
	private final Rectangle   _rOuter = new Rectangle();
	/** Array of square rectangles. */
	private final Rectangle[] _rSq    = new Rectangle[ 64 ];

	/** .T. if black is at top of board; .F. if black is at bottom. */
	private boolean _bBlackOnTop = true;
	/** .T. to display rank/file labels; .F. to hide them. */
	private boolean _bShowLabels = true;
	/** Dimension of squares, measured in pixels. */
	private int _iSqDim;

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	/**
	 * Default CTOR.
	 */
	public BoardView()
		{
		for ( int iSq = 0; iSq < 64; ++iSq )
			_rSq[ iSq ] = new Rectangle();

		initGUI();
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Renders the panel content.
	 *
	 * @param gfx
	 * 	Graphics context to draw into.
	 */
	@Override
	public void render( Graphics2D gfx, Rectangle rClip )
		{
		DBC.requireNotNull( gfx, "Graphics" );

		if (_rOuter.isEmpty())
			return;
		/*
		**	CODE
		*/
		ITheme theme = ThemeManager.getCurrentTheme();

		drawBoard( gfx, theme, rClip );
		drawLabels( gfx, theme, rClip );
		}

	//  -----------------------------------------------------------------------
	//	IMPLEMENTATION
	//	-----------------------------------------------------------------------

	/**
	 * Draws the board.
	 *
	 * @param gfx
	 * 	Graphics context to draw into.
	 * @param theme
	 * 	Theme.
	 * @param rClip
	 * 	Clip rectangle.
	 */
	private void drawBoard( Graphics2D gfx, final ITheme theme, final Rectangle rClip )
		{
		assert gfx != null;
		assert theme != null;
		assert rClip != null;
		/*
		**	CODE
		*/
		final Color clrDarkSq = theme.getDarkSquareColor();
		final Color clrLightSq = theme.getLightSquareColor();

		for ( int iSq = 0; iSq < 64; ++iSq )
			{
			final Rectangle rSq = new Rectangle( _rSq[ iSq ] );

			if (!rClip.intersects( rSq ))
				continue;    // don't draw obscured squares

			gfx.setColor( Square.isDark( iSq ) ? clrDarkSq : clrLightSq );
			gfx.fillRect( rSq.x, rSq.y, rSq.width, rSq.height );
/*
			rSq.grow( -GfxUtil.MARGIN_THIN, -GfxUtil.MARGIN_THIN );

			gfx.setColor( Color.black );
			gfx.drawString( Square.toString( iSq ), (int) rSq.getMinX(), (int) rSq.getMaxY() );
*/
			}

		gfx.setColor( Color.black );
		gfx.drawRect( _rInner.x, _rInner.y, _rInner.width, _rInner.height );
		}

	/**
	 * Draws the rank/file labels around the edge of the board.
	 *
	 * @param gfx
	 * 	Graphics context to draw into.
	 * @param theme
	 * 	Theme.
	 * @param rClip
	 * 	Clip rectangle.
	 */
	private void drawLabels( Graphics2D gfx, final ITheme theme, final Rectangle rClip )
		{
		assert gfx != null;
		assert theme != null;
		assert rClip != null;

		if (!_bShowLabels)
			return;
		/*
		**	CODE
		*/
		final float fFontSize = (float) (0.65 * (_iSqDim / 2));

		if (fFontSize < GfxUtil.MIN_FONT_SIZE)
			return;

		final Font fontOld = gfx.getFont();
		final Font font = theme.getLabelFont().deriveFont( fFontSize );
		final Rectangle rFile = new Rectangle( _iSqDim, (_iSqDim / 2) );
		final Rectangle rRank = new Rectangle( (_iSqDim / 2), _iSqDim );

		gfx.setFont( font );
		gfx.setColor( theme.getLabelColor() );

		for ( int idx = 0; idx < 8; ++idx )
			{
			final char cFile = (char) ('a' + getDisplayFile( idx ));
			final char cRank = (char) ('1' + getDisplayRank( idx ));

			// File label along top
			rFile.x = (int) (_rInner.getMinX() + (idx * _iSqDim));
			rFile.y = (int) _rOuter.getMinY();
			GfxUtil.drawCentered( gfx, rFile, cFile );

			// File label along bottom
			rFile.y = (int) _rInner.getMaxY();
			GfxUtil.drawCentered( gfx, rFile, cFile );

			// Rank label along left
			rRank.x = (int) _rOuter.getMinX();
			rRank.y = (int) (_rInner.getMinY() + (idx * _iSqDim));
			GfxUtil.drawCentered( gfx, rRank, cRank );

			// Rank label along right
			rRank.x = (int) _rInner.getMaxX();
			GfxUtil.drawCentered( gfx, rRank, cRank );
			}

		gfx.setFont( fontOld );
		}

	/**
	 * Creates all of the UI elements.
	 */
	private void initGUI()
		{
		addComponentListener( new ResizeListener() );

		setMinimumSize( new Dimension( (MIN_SQ_DIM * 9), (MIN_SQ_DIM * 9) ) );
		setPreferredSize( new Dimension( (DEFAULT_SQ_DIM * 9) + GfxUtil.MARGIN_THICK,
										 (DEFAULT_SQ_DIM * 9) + GfxUtil.MARGIN_THICK ) );
		//
		//	Context Menu
		//
		JPopupMenu popMenu = new JPopupMenu();

		popMenu.add( new FlipBoardCommand().createMenuItem( false ) );
		popMenu.add( new ShowLabelsCommand().createMenuItem( true ) );

		setComponentPopupMenu( popMenu );
		}

	/**
	 * Converts a file to it's display file, which respects the "Black on Top" flag.
	 *
	 * @param iFile
	 * 	Square.
	 *
	 * @return Display file [0..7].
	 */
	private int getDisplayFile( int iFile )
		{
		return _bBlackOnTop
			   ? iFile
			   : 7 - iFile;
		}

	/**
	 * Converts a rank to it's display rank, which respects the "Black on Top" flag.
	 *
	 * @param iRank
	 * 	Square.
	 *
	 * @return Display rank [0..7].
	 */
	private int getDisplayRank( int iRank )
		{
		return _bBlackOnTop
			   ? 7 - iRank
			   : iRank;
		}

	//  -----------------------------------------------------------------------
	//	NESTED CLASS: FlipBoardCommand
	//	-----------------------------------------------------------------------

	class FlipBoardCommand extends Command
		{
		public FlipBoardCommand()
			{
			putValue( NAME, "Flip Board" );
			putValue( LONG_DESCRIPTION, "Flips the board." );

			Image img = ResourceManager.getImage( "FlipBoard-16x16.png" );
			if (img != null)
				putValue( SMALL_ICON, new ImageIcon( img ) );
			}

		@Override
		public void run()
			{
			s_log.debug( "FlipBoardCommand" );
			/*
			**  CODE
            */
			_bBlackOnTop = !_bBlackOnTop;

			update();
			repaint();
			}
		}

	//  -----------------------------------------------------------------------
	//	NESTED CLASS: ShowLabelsCommand
	//	-----------------------------------------------------------------------

	class ShowLabelsCommand extends Command
		{
		public ShowLabelsCommand()
			{
			putValue( NAME, "Show Labels" );
			putValue( LONG_DESCRIPTION, "Shows or hides the rank/file labels." );
			}

		@Override
		public void run()
			{
			s_log.debug( "ShowLabelsCommand" );
			/*
			**  CODE
            */
			_bShowLabels = !_bShowLabels;

			update();
			repaint();
			}

		@Override
		public void update()
			{
			if (_item != null)
				_item.setSelected( _bShowLabels );
			}
		}

	//  -----------------------------------------------------------------------
	//	NESTED CLASS: ResizeListener
	//	-----------------------------------------------------------------------

	class ResizeListener extends ComponentAdapter
		{
		public void componentResized( ComponentEvent ce )
			{
			assert ce != null;

			if (getBounds().isEmpty())
				return;
			/*
			**	CODE
			*/
			final int iHeight = getHeight() - GfxUtil.MARGIN_THICK;
			final int iWidth = getWidth() - GfxUtil.MARGIN_THICK;
			final int iSqDim = Math.max( MIN_SQ_DIM, Math.min( iHeight, iWidth ) / 9 );

			Rectangle rOuter = new Rectangle( (iSqDim * 9), (iSqDim * 9) );
			GfxUtil.centerRectangle( rOuter, getBounds() );

			if (rOuter.equals( _rOuter ))
				return;    // nothing changed.

			_iSqDim = iSqDim;

			_rOuter.x = rOuter.x;
			_rOuter.y = rOuter.y;
			_rOuter.height = rOuter.height;
			_rOuter.width = rOuter.width;

			_rInner.height = _rInner.width = iSqDim * 8;
			GfxUtil.centerRectangle( _rInner, _rOuter );
			//
			//	Compute of all the squares.
			//
			final int iX = (int) _rInner.getMinX();
			final int iY = (int) _rInner.getMinY();

			for ( int iSq = 0; iSq < 64; ++iSq )
				{
				_rSq[ iSq ].x = iX + (iSqDim * getDisplayFile( Square.getFile( iSq ) ));
				_rSq[ iSq ].y = iY + (iSqDim * getDisplayRank( Square.getRank( iSq ) ));
				_rSq[ iSq ].width = _rSq[ iSq ].height = iSqDim;
				}

			//	s_log.debug( "BoardView resized squares to {} pixels.", iSqDim );
			}
		}
	}	/* end of class BoardView */
