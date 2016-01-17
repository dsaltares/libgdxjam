package com.siondream.libgdxjam.desktop;

import java.io.File;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import com.siondream.libgdxjam.Env;

/**
 * @brief 
 * RuntimeTexturePacker will explore your assets tree folder and generate an atlas (max. in android 2048 x 2048).
 * So output will be a .png and an .atlas
 * It is intended to be used only for debugging purposes on Desktop platform.
 * @author AlbertoCejas
 *
 */
public class RuntimeTexturePacker 
{

	public static final void generateAtlases(Settings settings)
	{
		// Clean atlas files if already exist
		System.out.println("--- Cleaning textures folders...");
		cleanFolder(Env.TEXTURES_FOLDER);
		
		// Process folders
		System.out.println("--- Packaging textures...");
		processFolder(settings, Env.UI_FOLDER);
		processFolder(settings, Env.TEXTURES_FOLDER);
		System.out.println("--- Packaging done");
	}
	
	private static final void cleanFolder(String path)
	{
		File folderToClean = new File(path);

		for(File childFile : folderToClean.listFiles())
		{
			if(childFile.isDirectory())
			{
				cleanFolder(childFile.getAbsolutePath());
			}
			// .png/.atlas to delete
			else if( (childFile.getName().compareTo(folderToClean.getName() + ".png") == 0)
					|| (childFile.getName().compareTo(folderToClean.getName() + ".atlas") == 0))
			{
				childFile.delete();
			}
		}
	}

	private static final void processFolder(Settings settings, String path)
	{
		File folderToProcess = new File(path);

		boolean folderRequiresProcess = false;

		for(File childFile : folderToProcess.listFiles())
		{
			if(childFile.isDirectory())
			{
				processFolder(settings, childFile.getAbsolutePath());
			}
			else
			{
				// It is a file, we need to pack!!
				folderRequiresProcess = true;
			}
		}
		
		// Perform actual pack now that we know that it's required
		if(folderRequiresProcess)
		{
			TexturePacker.process(settings, path, path, folderToProcess.getName());
		}
	}

}
