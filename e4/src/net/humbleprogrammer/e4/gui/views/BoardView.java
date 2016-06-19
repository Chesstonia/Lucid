/*****************************************************************************
 **
 ** @since 1.0
 **
 ******************************************************************************/
package net.humbleprogrammer.e4.gui.views;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.humbleprogrammer.e4.gui.helpers.Command;
import net.humbleprogrammer.e4.gui.themes.ThemeManager;
import net.humbleprogrammer.e4.interfaces.*;
import net.humbleprogrammer.humble.DBC;
import net.humbleprogrammer.humble.GfxUtil;
import net.humbleprogrammer.maxx.Board;
import net.humbleprogrammer.maxx.Square;

import static net.humbleprogrammer.maxx.Constants.*;

public class BoardView
	extends net.humbleprogrammer.e4.gui.controls.EnhancedPanel
	implements IBoardPresenter, Observer
	{

	//  -----------------------------------------------------------------------
	//	CONSTANTS
	//	-----------------------------------------------------------------------

	/** Default square size, measured in pixels. */
	private static final int DEFAULT_SQ_DIM = 48;
	/** Maximum square size, measured in pixels. */
	private static final int MAX_SQ_DIM     = 256;
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

	/** .T. of Black player is on top; .F. if White. */
	private boolean _bBlackOnTop     = true;
	/** .T. to display rank/file labels; .F. to hide them. */
	private boolean _bShowLabels     = true;
	/** .T. to highlight legal moves; .F. otherwise. */
	private boolean _bShowLegalMoves = true;
	/** Dimension of squares, measured in pixels. */
	private int     _iSqDim          = -1;
	/** Bitboard of enabled squares. */
	private long             _bbEnabled;
	/** Bitboard of selected squares. */
	private long             _bbSelected;
	/** Board controller. */
	private IBoardController _controller;
	/** Current piece set. */
	private Image            _imgPieces;

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

		createUI();
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
		/*
		**	CODE
		*/
		final ITheme theme = ThemeManager.getCurrentTheme();

		adjustLayout();

		drawBoard( gfx, theme, rClip );
		drawLabels( gfx, theme, rClip );

		if (_controller != null)
			{
			final Board bd = _controller.getPosition();

			drawMoveIndicator( gfx, theme, rClip, (bd.getMovingPlayer() == WHITE) );
			drawPieces( gfx, theme, rClip, bd );
			}
		}

	//  -----------------------------------------------------------------------
	//	INTERFACE: IBoardPresenter
	//	-----------------------------------------------------------------------

	/**
	 * Gets the "Show Legal Moves" flag.
	 *
	 * @return .T. if legal moves are displayed; .F. otherwise.
	 */
	@Override
	public boolean getShowLegalMoves()
		{
		return _bShowLegalMoves;
		}

	/**
	 * Sets the board controller.
	 *
	 * @param controller
	 * 	Controller, or null for no control.
	 */
	@Override
	public void setBoardController( IBoardController controller )
		{
		if (_controller == controller)
			return;
		/*
		**	CODE
		*/
		if ((_controller = controller) != null)
			_controller.setBoardPresenter( this );

		_bbEnabled = _bbSelected = 0L;
		repaint();
		}

	/**
	 * Enables squares.
	 *
	 * @param bbEnabled
	 * 	Bitboard of squares to enable.
	 */
	@Override
	public void setEnabled( long bbEnabled )
		{
		if (bbEnabled != _bbEnabled)
			{
			_bbEnabled = bbEnabled;
			repaint();
			}
		}

	/**
	 * Selects squares.
	 *
	 * @param bbSelected
	 * 	Bitboard of squares to select.
	 */
	@Override
	public void setSelection( long bbSelected )
		{
		if (bbSelected != _bbSelected)
			{
			_bbSelected = bbSelected;
			repaint();
			}
		}
	//  -----------------------------------------------------------------------
	//	INTERFACE: Observer
	//	-----------------------------------------------------------------------

	/**
	 * Called when the document object is updated.
	 *
	 * @param obj
	 * 	Document object.
	 * @param arg
	 * 	(not used)
	 */
	@Override
	public void update( Observable obj, Object arg )
		{
		assert obj == _controller;
		/*
		**	CODE
		*/
		repaint();
		}

	//  -----------------------------------------------------------------------
	//	IMPLEMENTATION
	//	-----------------------------------------------------------------------

	/**
	 * Resizes all the squares.
	 */
	private void adjustLayout()
		{
		final int iHeight = getHeight() - GfxUtil.MARGIN_THICK;
		final int iWidth = getWidth() - GfxUtil.MARGIN_THICK;
		final int iSqDim = Math.max( MIN_SQ_DIM, Math.min( iHeight, iWidth ) / 9 );

		if (iSqDim == _iSqDim)
			{
			Rectangle rOuter = new Rectangle( (iSqDim * 9), (iSqDim * 9) );
			GfxUtil.centerRectangle( rOuter, getBounds() );

			if (rOuter.equals( _rOuter ))
				return;
			}
		else
			{
			_iSqDim = iSqDim;
			_imgPieces = null;

			s_log.debug( "BoardView => square size is {} pixels.", iSqDim );
			}

		_rOuter.setSize( (iSqDim * 9), (iSqDim * 9) );
		GfxUtil.centerRectangle( _rOuter, getBounds() );

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
		}

	/**
	 * Creates all of the UI elements.
	 */
	private void createUI()
		{
		setMaximumSize( new Dimension( GfxUtil.MARGIN_THICK + (MAX_SQ_DIM * 9),
									   GfxUtil.MARGIN_THICK + (MAX_SQ_DIM * 9) ) );
		setMinimumSize( new Dimension( GfxUtil.MARGIN_THICK + (MIN_SQ_DIM * 9),
									   GfxUtil.MARGIN_THICK + (MIN_SQ_DIM * 9) ) );
		setPreferredSize( new Dimension( GfxUtil.MARGIN_THICK + (DEFAULT_SQ_DIM * 9),
										 GfxUtil.MARGIN_THICK + (DEFAULT_SQ_DIM * 9) ) );
		//
		//	Context Menu
		//
		JPopupMenu popMenu = new JPopupMenu();

		popMenu.add( cmdToggleOnTop.createMenuItem() );
		popMenu.add( cmdShowLabels.createCheckedMenuItem() );
		popMenu.add( cmdShowLegalMoves.createCheckedMenuItem() );

		setComponentPopupMenu( popMenu );
		}

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

		if (rClip.isEmpty())
			return;
		/*
		**	CODE
		*/
		final Color clrDarkSq = theme.getDarkSquareColor();
		final Color clrLightSq = theme.getLightSquareColor();

		for ( int iSq = 0; iSq < 64; ++iSq )
			if (rClip.intersects( _rSq[ iSq ] ))
				{
				gfx.setColor( Square.isDark( iSq ) ? clrDarkSq : clrLightSq );
				gfx.fillRect( _rSq[ iSq ].x, _rSq[ iSq ].y, _iSqDim, _iSqDim );
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
	 * 	Current theme.
	 * @param rClip
	 * 	Clip rectangle.
	 */
	private void drawLabels( Graphics2D gfx, final ITheme theme, final Rectangle rClip )
		{
		assert gfx != null;
		assert theme != null;
		assert rClip != null;

		if (!_bShowLabels || rClip.isEmpty())
			return;
		/*
		**	CODE
		*/
		final int iHalfSq = _iSqDim / 2;
		final float fFontSize = (float) (0.65 * iHalfSq);

		if (fFontSize < GfxUtil.MIN_FONT_SIZE)
			return;

		final Font fontOld = gfx.getFont();

		try
			{
			final Rectangle rFile = new Rectangle( _iSqDim, iHalfSq );
			final Rectangle rRank = new Rectangle( iHalfSq, _iSqDim );

			gfx.setFont( theme.getLabelFont().deriveFont( fFontSize ) );
			gfx.setColor( theme.getLabelColor() );

			for ( int idx = 0; idx < 8; ++idx )
				{
				final char cFile = (char) ('a' + getDisplayFile( idx ));
				final char cRank = (char) ('1' + getDisplayRank( idx ));

				// File label along top
				rFile.x = (int) _rInner.getMinX() + (idx * _iSqDim);
				rFile.y = (int) _rOuter.getMinY();
				if (rClip.intersects( rFile ))
					GfxUtil.drawCentered( gfx, rFile, cFile );

				// File label along bottom
				rFile.y = (int) _rInner.getMaxY();
				if (rClip.intersects( rFile ))
					GfxUtil.drawCentered( gfx, rFile, cFile );

				// Rank label along left
				rRank.x = (int) _rOuter.getMinX();
				rRank.y = (int) _rInner.getMinY() + (idx * _iSqDim);
				if (rClip.intersects( rRank ))
					GfxUtil.drawCentered( gfx, rRank, cRank );

				// Rank label along right
				rRank.x = (int) _rInner.getMaxX();
				if (rClip.intersects( rRank ))
					GfxUtil.drawCentered( gfx, rRank, cRank );
				}
			}
		finally
			{
			gfx.setFont( fontOld );
			}
		}

	/**
	 * Draws the move indicator.
	 *
	 * @param gfx
	 * 	Graphics context to draw into.
	 * @param theme
	 * 	Theme.
	 * @param rClip
	 * 	Clip rectangle.
	 * @param bWhiteToMove
	 * 	.T. if the White player is moving; .F. if Black player.
	 */
	private void drawMoveIndicator( Graphics2D gfx, final ITheme theme, final Rectangle rClip,
									boolean bWhiteToMove )
		{
		assert gfx != null;
		assert theme != null;
		assert rClip != null;

		if (rClip.isEmpty())
			return;
		/*
		**	CODE
		*/
		final boolean bAtTop = bWhiteToMove ^ _bBlackOnTop;
		final Rectangle rCorner = new Rectangle( (_iSqDim / 2), (_iSqDim / 2) );
		final Rectangle rMarker = new Rectangle( (int) (0.75 * rCorner.width),
												 (int) (0.90 * rCorner.height) );

		rCorner.x = (int) _rInner.getMaxX();
		rCorner.y = (int) (bAtTop ? _rOuter.getMinY() : _rInner.getMaxY());

		GfxUtil.centerRectangle( rMarker, rCorner );

		if (rMarker.isEmpty() || !rClip.intersects( rMarker ))
			return;
		//
		//	Draw a "home plate" shaped indicator.
		//
		final int iT = (int) rMarker.getMinY();    // top
		final int iL = (int) rMarker.getMinX();    // left
		final int iB = (int) rMarker.getMaxY();    // bottom
		final int iR = (int) rMarker.getMaxX();    // right

		final int iMidX = rMarker.width / 2;
		final int iMidY = rMarker.height / 2;

		GeneralPath path = new GeneralPath();

		if (bAtTop)
			{
			path.moveTo( iL + iMidX, iB );
			path.lineTo( iL, iB - iMidY );
			path.lineTo( iL, iT );
			path.lineTo( iR, iT );
			path.lineTo( iR, iB - iMidY );
			path.lineTo( iL + iMidX, iB );
			}
		else
			{
			path.moveTo( iL + iMidX, iT );
			path.lineTo( iR, iT + iMidY );
			path.lineTo( iR, iB );
			path.lineTo( iL, iB );
			path.lineTo( iL, iT + iMidY );
			path.lineTo( iL + iMidX, iT );
			}

		path.closePath();

		if (bWhiteToMove)
			gfx.setColor( Color.WHITE );
		else
			gfx.setColor( Color.BLACK );

		gfx.fill( path );
		gfx.setColor( Color.DARK_GRAY );
		gfx.draw( path );
		}

	/**
	 * Draws the move indicator.
	 *
	 * @param gfx
	 * 	Graphics context to draw into.
	 * @param theme
	 * 	Theme.
	 * @param rClip
	 * 	Clip rectangle.
	 * @param bd
	 * 	Board object.
	 */
	private void drawPieces( Graphics2D gfx, final ITheme theme, final Rectangle rClip,
							 final Board bd )
		{
		assert gfx != null;
		assert theme != null;
		assert rClip != null;
		assert bd != null;
		/*
		**	CODE
		*/
		if (_imgPieces == null)
			_imgPieces = theme.getPieceSet( _iSqDim );

		if (_imgPieces == null || rClip.isEmpty())
			return;

		for ( int iSq = 0; iSq < 64; ++iSq )
			{
			final int piece = bd.get( iSq );
			final Rectangle rSq = new Rectangle( _rSq[ iSq ] );

			if (piece != EMPTY && rClip.intersects( rSq ))
				{
				//
				//	Compute the offset into the piece bitmap, which expects the White pieces on the top
				//	row, and the Black pieces on the bottom row.  Each row then has the pieces ordered
				//	Pawn, Knight, Bishop, Rook, Queen, King.
				//
				Point ptSrc = new Point( _iSqDim * ((piece - MAP_W_PAWN) >> 1),
										 _iSqDim * (piece & 1) );

				gfx.drawImage( _imgPieces,
							   rSq.x, rSq.y, rSq.x + _iSqDim, rSq.y + _iSqDim,
							   ptSrc.x, ptSrc.y, ptSrc.x + _iSqDim, ptSrc.y + _iSqDim,
							   null );

				}
			}
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
		return _bBlackOnTop ? iFile : (7 - iFile);
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
		return _bBlackOnTop ? (7 - iRank) : iRank;
		}

	//  -----------------------------------------------------------------------
	//	COMMAND: BlackOnTopCommand
	//	-----------------------------------------------------------------------

	@SuppressWarnings( "unused" )
	private final Command cmdBlackOnTop = new Command( Command.ID.BLACK_ON_TOP,
													   "Black on Top",
													   "Places the Black player at the top of the board." )
	{
	@Override
	public void run()
		{
		s_log.debug( "cmdBlackOnTop" );
		/*
		**	CODE
		*/
		_bBlackOnTop = true;
		_iSqDim = -1;    // force recalculation of all the squares.

		repaint();
		}
	};

	//  -----------------------------------------------------------------------
	//	COMMAND: ToggleOnTopCommand
	//	-----------------------------------------------------------------------

	private final Command cmdToggleOnTop = new Command( Command.ID.TOGGLE_ON_TOP,
														"Flip Board",
														"Switches the top and bottom players.",
														"FlipBoard-16x16.png" )
	{
	@Override
	public void run()
		{
		s_log.debug( "cmdToggleOnTop" );
		/*
		**	CODE
		*/
		_bBlackOnTop = !_bBlackOnTop;
		_iSqDim = -1;    // force recalculation of all the squares.

		repaint();
		}
	};

	//  -----------------------------------------------------------------------
	//	COMMAND: WhiteOnTopCommand
	//	-----------------------------------------------------------------------

	@SuppressWarnings( "unused" )
	private final Command cmdWhiteOnTop = new Command( Command.ID.WHITE_ON_TOP,
													   "White on Top",
													   "Places the White player at the top of the board." )
	{
	@Override
	public void run()
		{
		s_log.debug( "cmdWhiteOnTop" );
		/*
		**	CODE
		*/
		_bBlackOnTop = false;
		_iSqDim = -1;    // force recalculation of all the squares.

		repaint();
		}
	};

	//  -----------------------------------------------------------------------
	//	COMMAND: ShowLabelsCommand
	//	-----------------------------------------------------------------------

	private final Command cmdShowLabels = new Command( null,
													   "Show Labels",
													   "Shows or hides the rank & file labels." )
	{
	@Override
	public void run()
		{
		s_log.debug( "cmdShowLabels" );
		/*
		**	CODE
		*/
		_bShowLabels = !_bShowLabels;

		repaint();
		update();
		}

	@Override
	public void update()
		{
		setChecked( _bShowLabels );
		}
	};

	//  -----------------------------------------------------------------------
	//	COMMAND: ShowLegalMovesCommand
	//	-----------------------------------------------------------------------

	private final Command cmdShowLegalMoves = new Command( null,
														   "Show Legal Moves",
														   "Shows all legal moves for a selected piece." )
	{
	@Override
	public void run()
		{
		s_log.debug( "cmdSHowLegalMoves" );
		/*
		**	CODE
		*/
		_bShowLegalMoves = !_bShowLegalMoves;

		repaint();
		update();
		}

	@Override
	public void update()
		{
		setChecked( _bShowLegalMoves );
		}
	};
	}	/* end of class BoardView */
/*****************************************************************************
 **
 ** @author Lee Neuse (coder@humbleprogrammer.net)
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
 **	other parties provide the program "as is" without warranty of any kind,
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
