package redempt.graphmaker;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;

public class GraphMaker {
	
	private static final Color[] lineColors = new Color[] {Color.RED, Color.BLUE, Color.GREEN, Color.CYAN, Color.PINK, Color.ORANGE, Color.GRAY};
	private static final DecimalFormat format = new DecimalFormat("#.##");
	
	public static void generateGraph(double scale, String... expressions) throws Exception {
		int color = 0;
		BufferedImage image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setBackground(Color.WHITE);
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, 500, 500);
		graphics.setColor(Color.BLACK);
		graphics.drawLine(250, 0, 250, 500);
		graphics.drawLine(0, 250, 500, 250);
		for (String expression : expressions) {
			Evaluator eval = new Evaluator(expression);
			double lastOutput = Double.MIN_VALUE;
			graphics.setColor(lineColors[color % lineColors.length]);
			for (int x = -250; x <= 250; x++) {
				double input = x * scale;
				double output = eval.evaluate(input);
				if (Double.isNaN(output)) {
					lastOutput = Double.MIN_VALUE;
					continue;
				}
				if (lastOutput != Double.MIN_VALUE) {
					graphics.drawLine(getX(x), getY(output, scale), getX(x - 1), getY(lastOutput, scale));
				}
				lastOutput = output;
			}
			color++;
		}
		graphics.setColor(Color.BLACK);
		graphics.drawString("0", 250, 250);
		graphics.drawString(format.format(250 * scale), 500 - graphics.getFontMetrics().stringWidth(format.format(250 * scale)), 250);
		graphics.drawString(format.format(250 * scale), 250, 10);
		graphics.dispose();
		ImageIO.write(image, "png", new File("graph.png"));
	}
	
	private static int getY(double y, double scale) {
		return (int) Math.round(250 - y / scale);
	}
	
	private static int getX(double x) {
		return (int) Math.round(x) + 250;
	}
	
}
