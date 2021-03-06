package examples;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class AnimationDoubleBuffering {
  private static final int IMAGE_WIDTH = 100;
  private static final int TIMER_INTERVAL = 10;

  private static int x = 0;
  private static int y = 0;

  private static int directionX = 1;
  private static int directionY = 1;

  private static Canvas canvas;

  public static void animate() {
    x += directionX;
    y += directionY;

    // Determine out of bounds
    Rectangle rect = canvas.getClientArea();
    if (x < 0) {
      x = 0;
      directionX = 1;
    } else if (x > rect.width - IMAGE_WIDTH) {
      x = rect.width - IMAGE_WIDTH;
      directionX = -1;
    }
    if (y < 0) {
      y = 0;
      directionY = 1;
    } else if (y > rect.height - IMAGE_WIDTH) {
      y = rect.height - IMAGE_WIDTH;
      directionY = -1;
    }

    // Force a redraw
    canvas.redraw();
  }

  public static void main(String[] args) {
    final Display display = new Display();
    final Shell shell = new Shell(display);
    shell.setText("Animator");

    shell.setLayout(new FillLayout());
    canvas = new Canvas(shell, SWT.NO_BACKGROUND);
    canvas.addPaintListener(new PaintListener() {
      public void paintControl(PaintEvent event) {
        // Create the image to fill the canvas
        Image image = new Image(shell.getDisplay(), canvas.getBounds());
        // Set up the offscreen gc
        GC gcImage = new GC(image);

        gcImage.setBackground(event.gc.getBackground());
        gcImage.fillRectangle(image.getBounds());
        gcImage.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_RED));
        gcImage.fillOval(x, y, IMAGE_WIDTH, IMAGE_WIDTH);

        // Draw the offscreen buffer to the screen
        event.gc.drawImage(image, 0, 0);

        image.dispose();
        gcImage.dispose();
      }
    });

    
    shell.open();
    Runnable runnable = new Runnable() {
      public void run() {
        animate();
        display.timerExec(TIMER_INTERVAL, this);
      }
    };
    display.timerExec(TIMER_INTERVAL, runnable);

    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        display.sleep();
      }
    }
    // Kill the timer
    display.timerExec(-1, runnable);
    display.dispose();

  }
}