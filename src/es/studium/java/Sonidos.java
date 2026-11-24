package es.studium.java;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class Sonidos
{
	private static Clip musica;

	public static void iniciarMusica()
	{
		File fichero = new File("sonidos/musica.wav");
		try
		{
			AudioInputStream audio = AudioSystem.getAudioInputStream(fichero);
			musica = AudioSystem.getClip();
			musica.open(audio);
			if (musica.isControlSupported(FloatControl.Type.MASTER_GAIN))
			{
				FloatControl volumen = (FloatControl) musica.getControl(FloatControl.Type.MASTER_GAIN);
				volumen.setValue(-12.0f);
			}
			musica.loop(Clip.LOOP_CONTINUOUSLY);
		}
		catch (Exception e)
		{
			System.out.println("No se pudo reproducir la música: " + e);
			System.out.println("Java la busca en: " + fichero.getAbsolutePath());
		}
	}

	public static void efecto(String nombre)
	{
		HiloSonido hilo = new HiloSonido(nombre);
		hilo.start();
	}
}