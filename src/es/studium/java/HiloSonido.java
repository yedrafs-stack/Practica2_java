package es.studium.java;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class HiloSonido extends Thread
{
	private String nombre;

	public HiloSonido(String nombre)
	{
		this.nombre = nombre;
		setDaemon(true);
	}

	public void run()
	{
		File fichero = new File("sonidos/" + nombre);
		try
		{
			AudioInputStream audio = AudioSystem.getAudioInputStream(fichero);
			Clip clip = AudioSystem.getClip();
			clip.open(audio);
			clip.start();
			Thread.sleep(clip.getMicrosecondLength() / 1000 + 100);
			clip.close();
		}
		catch (Exception e)
		{
			System.out.println("No se pudo reproducir " + nombre + ": " + e);
		}
	}
}