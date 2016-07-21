package net.humbleprogrammer.toolbox;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IllegalFormatException;
import java.util.List;

import org.slf4j.*;

public abstract class ToolboxApp
	{

	//  -----------------------------------------------------------------------
	//	STATIC DECLARATIONS
	//	-----------------------------------------------------------------------

	/** Logger */
	protected static final Logger s_log = LoggerFactory.getLogger(ToolboxApp.class);

	//  -----------------------------------------------------------------------
	//	IMPLEMENTATION
	//	-----------------------------------------------------------------------

	/**
	 * Builds a set of *.pgn files.
	 *
	 * @return Count of files found.
	 */
	protected static List<Path> getPGN( String strPath )
		{
		List<Path> list = new ArrayList<>();

		try
			{
			Path pathPgnRoot = Paths.get(strPath);
			DirectoryStream<Path> stream = Files.newDirectoryStream(pathPgnRoot, "*.pgn");

			for ( Path path : stream )
				list.add(path);

			stream.close();
			Collections.sort(list);

			s_log.debug("Found {} PGN files.", list.size());
			}
		catch ( Exception ex )
			{
			s_log.warn("Failed to find PGN files: {}", ex.getMessage());
			}

		return list;
		}

	protected void print( String strText )
		{
		if (strText == null) return;
		//	-----------------------------------------------------------------
		System.out.print(strText);
		}

	protected void print( String strFormat, Object... args )
		{
		if (strFormat == null) return;
		//	-----------------------------------------------------------------
		try
			{
			System.out.print(String.format(strFormat, args));
			}
		catch ( IllegalFormatException ex )
			{
			s_log.warn(String.format("Invalid format string '{}'", strFormat), ex);
			}
		}

	protected void printLine( String strText )
		{
		if (strText == null) return;
		//	-----------------------------------------------------------------
		System.out.println(strText);
		}

	protected void printLine( String strFormat, Object... args )
		{
		if (strFormat == null) return;
		//	-----------------------------------------------------------------
		try
			{
			System.out.println(String.format(strFormat, args));
			}
		catch ( IllegalFormatException ex )
			{
			s_log.warn(String.format("Invalid format string '{}'", strFormat), ex);
			}
		}
	}
