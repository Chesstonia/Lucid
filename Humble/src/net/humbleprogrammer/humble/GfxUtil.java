/* ****************************************************************************
 *
 *	@author Lee Neuse (coder@humbleprogrammer.net)
 *	@since 1.0
 *
 *	---------------------------- [License] ----------------------------------
 *	This work is licensed under the Creative Commons Attribution-NonCommercial-
 *	ShareAlike 3.0 Unported License. To view a copy of this license, visit
 *			http://creativecommons.org/licenses/by-nc-sa/3.0/
 *	or send a letter to Creative Commons, 444 Castro Street Suite 900, Mountain
 *	View, California, 94041, USA.
 *	--------------------- [Disclaimer of Warranty] --------------------------
 *	There is no warranty for the program, to the extent permitted by applicable
 *	law.  Except when otherwise stated in writing the copyright holders and/or
 *	other parties provide the program "as is" without warranty of any kind,
 *	either expressed or implied, including, but not limited to, the implied
 *	warranties of merchantability and fitness for a particular purpose.  The
 *	entire risk as to the quality and performance of the program is with you.
 *	Should the program prove defective, you assume the cost of all necessary
 *	servicing, repair or correction.
 *	-------------------- [Limitation of Liability] --------------------------
 *	In no event unless required by applicable law or agreed to in writing will
 *	any copyright holder, or any other party who modifies and/or conveys the
 *	program as permitted above, be liable to you for damages, including any
 *	general, special, incidental or consequential damages arising out of the
 *	use or inability to use the program (including but not limited to loss of
 *	data or data being rendered inaccurate or losses sustained by you or third
 *	parties or a failure of the program to operate with any other programs),
 *	even if such holder or other party has been advised of the possibility of
 *	such damages.
 *
 ******************************************************************************/
package net.humbleprogrammer.humble;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GfxUtil
	{

	//  -----------------------------------------------------------------------
	//	CONSTANTS
	//	-----------------------------------------------------------------------

	/** Normal margin, measured in pixels. */
	public static final int MARGIN       = 8;
	/** Wide margin, measured in pixels. */
	public static final int MARGIN_THICK = 12;
	/** Narrow margin, measured in pixels. */
	public static final int MARGIN_THIN  = 4;

	/** Minimum font size, in points. */
	public static final float MIN_FONT_SIZE = 6F;

	//  -----------------------------------------------------------------------
	//	STATIC DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Controls the rendering quality */
	protected static final RenderingHints s_hints;

	//  -----------------------------------------------------------------------
	//	CTOR
	//	-----------------------------------------------------------------------

	static
		{
		s_hints = new RenderingHints( RenderingHints.KEY_ANTIALIASING,
									  RenderingHints.VALUE_ANTIALIAS_ON );
		s_hints.put( RenderingHints.KEY_INTERPOLATION,
					 RenderingHints.VALUE_INTERPOLATION_BILINEAR );
		s_hints.put( RenderingHints.KEY_TEXT_ANTIALIASING,
					 RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC METHODS
	//	-----------------------------------------------------------------------

	/**
	 * Adjusts inner rectangle so that it is centered in the outer rectangle.
	 *
	 * @param rInner
	 * 	Inner rectangle.
	 * @param rOuter
	 * 	Outer rectangle.
	 */
	public static void centerRectangle( Rectangle rInner, final Rectangle rOuter )
		{
		double dOffsetX = (rOuter.width - rInner.width) / 2.0;
		double dOffsetY = (rOuter.height - rInner.height) / 2.0;

		rInner.setLocation( (int) (rOuter.getMinX() + dOffsetX),
							(int) (rOuter.getMinY() + dOffsetY) );
		}

	/**
	 * Draws a character centered inside a rectangle.
	 *
	 * @param gfx
	 * 	Graphics context to draw into.
	 * @param rBounds
	 * 	Bounding rectangle.
	 * @param ch
	 * 	Character to draw.
	 */
	public static void drawCentered( Graphics2D gfx, final Rectangle rBounds, char ch )
		{
		DBC.requireNotNull( gfx, "Graphics" );
		DBC.requireNotNull( rBounds, "Bounds" );
		DBC.requireGreaterThanZero( ch, "Character" );
		//	-----------------------------------------------------------------
		final FontMetrics fm = gfx.getFontMetrics();
		final String strText = String.valueOf( ch );

		int iOffsetX = (rBounds.width - fm.stringWidth( strText )) / 2;
		int iOffsetY = ((rBounds.height - fm.getHeight()) / 2) + fm.getAscent();

		gfx.drawString( strText, (rBounds.x + iOffsetX), (rBounds.y + iOffsetY) );
		}

	/**
	 * Resizes an image.
	 *
	 * Source: http://stackoverflow.com/a/24746194/1410992
	 *
	 * @param image
	 * 	Image to resize.
	 * @param dim
	 * 	New size.
	 * @param bHighQuality
	 * 	.T. for slower, higher-quality scaling; .F. for faster, lower-quality scaling.
	 */
	public static BufferedImage scaleImage( BufferedImage image, Dimension dim, boolean bHighQuality )
		{
		DBC.requireNotNull( image, "Image" );
		DBC.requireNotNull( dim, "Dimension" );

		assert dim.height > 0;
		assert dim.width > 0;
		//	-----------------------------------------------------------------
		int iHeight;
		int iWidth;
		BufferedImage imgScaled = image;

		if (bHighQuality)
			{
			//
			//	Use multi-step technique: start with original size, then scale down in multiple
			//	passes with drawImage() until the target size is reached.
			//
			iWidth = image.getWidth( null );
			iHeight = image.getHeight( null );
			}
		else
			{
			//
			//	Use one-step technique: scale directly from original size to target size with a
			//	single drawImage() call.
			//
			iWidth = dim.width;
			iHeight = dim.height;
			}

		do
			{
			if (iWidth > dim.width)
				iWidth = Math.max( dim.width, (iWidth / 2) );

			if (iHeight > dim.height)
				iHeight = Math.max( dim.height, (iHeight / 2) );

			BufferedImage imgTmp = new BufferedImage( iWidth, iHeight,
													  BufferedImage.TYPE_INT_ARGB );
			Graphics2D gfx2 = imgTmp.createGraphics();

			try
				{
				gfx2.setRenderingHints( s_hints );
				gfx2.drawImage( imgScaled, 0, 0, iWidth, iHeight, null );
				}
			finally
				{
				gfx2.dispose();
				}

			imgScaled = imgTmp;
			}
		while ( iWidth != dim.width || iHeight != dim.height );

		return imgScaled;
		}

	//  -----------------------------------------------------------------------
	//	PUBLIC GETTERS & SETTERS
	//	-----------------------------------------------------------------------

	/**
	 * Gets high-quality rendering hints.
	 *
	 * @return Hints.
	 */
	public static RenderingHints getQualityRenderingHints()
		{
		return s_hints;
		}
	}   /* end of class GfxUtil */
