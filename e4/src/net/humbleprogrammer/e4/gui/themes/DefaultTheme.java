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
package net.humbleprogrammer.e4.gui.themes;

import java.awt.*;

import net.humbleprogrammer.e4.gui.helpers.ResourceManager;
import net.humbleprogrammer.humble.DBC;

import static net.humbleprogrammer.maxx.Constants.*;

//
//	Named colors: http://www.imagemagick.org/script/color.php
//
public class DefaultTheme implements ITheme
	{
	private static final Color s_clrDarkSq  = new Color( 0x556B2F );    // dark olive green
	private static final Color s_clrLabels  = new Color( 0x333333 );    // grey 20
	private static final Color s_clrLightSq = new Color( 0xFFF8DC );    // cornsilk
	/** Default piece set. */
	private static final Image s_imgPiecesLarge;
	/** Default piece set. */
	private static final Image s_imgPiecesSmall;

	/** Default label font. */
	private static Font s_fontLabels;

	static
		{
		s_imgPiecesLarge = ResourceManager.getImage( "PiecesUSCF-96x96.png" );
		s_imgPiecesSmall = ResourceManager.getImage( "PiecesUSCF-32x32.png" );
		}

	@Override
	public Color getDarkSquareColor()
		{
		return s_clrDarkSq;
		}

	@Override
	public Color getLabelColor()
		{
		return s_clrLabels;
		}

	/**
	 * Gest the font to use to draw rank/file labels.
	 *
	 * @return Font object.
	 */
	@Override
	public Font getLabelFont()
		{
		if (s_fontLabels == null)
			s_fontLabels = new Font( "Segoe UI", Font.BOLD, 12 );

		return s_fontLabels;
		}

	@Override
	public Color getLightSquareColor()
		{
		return s_clrLightSq;
		}

	/**
	 * Gets the piece set, scaled to a square size.
	 *
	 * @param iSqDim
	 * 	Desired square size, in pixels.
	 *
	 * @return Scaled piece set image.
	 */
	@Override
	public Image getPieceSet( int iSqDim )
		{
		DBC.requireGreaterThanZero( iSqDim, "Square size" );
		/*
		**	CODE
		*/
		int iHeight = iSqDim * 2;
		int iWidth = iSqDim * 6;

		return (iSqDim >= (96 / 2))
			   ? s_imgPiecesLarge.getScaledInstance( iWidth, iHeight, Image.SCALE_SMOOTH )
			   : s_imgPiecesSmall.getScaledInstance( iWidth, iHeight, Image.SCALE_SMOOTH );
		}

	}
