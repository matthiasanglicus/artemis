import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Assign2UI extends JFrame {
	JPopupMenu viewportPopup;
	Assign2 imgProcessor;
	BufferedImage img;
	ArrayList<double[]> corners = new ArrayList<double[]>();
	public Assign2UI() {
		super("COMP 7502 - Assignment 2");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JScrollPane scroller = new JScrollPane(new ImagePanel());
		this.add(scroller);
		this.setSize(750, 600);
		this.setVisible(true);
	}

	public static void main(String args[]) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Assign2UI();
			}
		});
	}

	class ImagePanel extends JPanel implements MouseListener, ActionListener {
		public ImagePanel() {
			imgProcessor = new Assign2();
			this.addMouseListener(this);
		}

		public Dimension getPreferredSize() {
			if (img != null) {
				return (new Dimension(img.getWidth(), img.getHeight()));
			} else {
				return (new Dimension(0, 0));
			}
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (img != null) {
				g.drawImage(img, 0, 0, this);
				renderCorners((Graphics2D)g);
			}
		}
		
		public void renderCorners(Graphics2D g2d) {
			double crossLength = 2.0;
			g2d.setColor(Color.RED);
			if (corners!=null) {
				for (double[] p : corners) {
					Line2D l = new Line2D.Double(p[0]-crossLength, p[1]-crossLength, p[0]+crossLength, p[1]+crossLength);
					g2d.draw(l);
					l = new Line2D.Double(p[0]-crossLength, p[1]+crossLength, p[0]+crossLength, p[1]-crossLength);
					g2d.draw(l);
				}
			}
		}
		

		private void showPopup(MouseEvent e) {
			JPopupMenu.setDefaultLightWeightPopupEnabled(false);
			viewportPopup = new JPopupMenu();

			JMenuItem openImageMenuItem = new JMenuItem("open image ...");
			openImageMenuItem.addActionListener(this);
			openImageMenuItem.setActionCommand("open image");
			viewportPopup.add(openImageMenuItem);

			JMenuItem loadDefaultImageMenuItem = new JMenuItem("load default image");
			loadDefaultImageMenuItem.addActionListener(this);
			loadDefaultImageMenuItem.setActionCommand("load default image");
			viewportPopup.add(loadDefaultImageMenuItem);

			viewportPopup.addSeparator();

			JMenuItem detectCornorsMenuItem = new JMenuItem("detect corners");
			detectCornorsMenuItem.addActionListener(this);
			detectCornorsMenuItem.setActionCommand("detect corners");
			viewportPopup.add(detectCornorsMenuItem);
			
			JMenuItem clearCornorsMenuItem = new JMenuItem("clear corners");
			clearCornorsMenuItem.addActionListener(this);
			clearCornorsMenuItem.setActionCommand("clear corners");
			viewportPopup.add(clearCornorsMenuItem);

			viewportPopup.addSeparator();

			JMenuItem exitMenuItem = new JMenuItem("exit");
			exitMenuItem.addActionListener(this);
			exitMenuItem.setActionCommand("exit");
			viewportPopup.add(exitMenuItem);

			viewportPopup.show(e.getComponent(), e.getX(), e.getY());
		}

		public void mouseClicked(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}

		public void mousePressed(MouseEvent e) {
			if (viewportPopup != null) {
				viewportPopup.setVisible(false);
				viewportPopup = null;
			} else {
				showPopup(e);
			}
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("open image")) {
				final JFileChooser fc = new JFileChooser();
				FileFilter imageFilter = new FileNameExtensionFilter("Image files", "bmp", "gif", "jpg");
				fc.addChoosableFileFilter(imageFilter);
				fc.setDragEnabled(true);
				fc.setMultiSelectionEnabled(false);
				fc.showOpenDialog(this);
				File file = fc.getSelectedFile();
				try {
					img = ImageIO.read(file);
					img = colorToGray(img);
				} catch (Exception ee) {
				}
			} else if (e.getActionCommand().equals("load default image")) {
				try {
					img = ImageIO.read(new URL("http://www.cs.hku.hk/~sdirk/sample2.jpg"));
					img = colorToGray(img);
				} catch (Exception ee) {
					JOptionPane.showMessageDialog(this, "Unable to fetch image from URL", "Error",
							JOptionPane.ERROR_MESSAGE);
					ee.printStackTrace();
				}
			} else if (e.getActionCommand().equals("detect corners")) {
				if (img != null) {
					new DetectCornersGUI().createAndShowGUI();
				}
			} else if (e.getActionCommand().equals("clear corners")) {
				corners = new ArrayList<double[]>();
			} else if (e.getActionCommand().equals("exit")) {
				System.exit(0);
			}
			viewportPopup = null;
			this.updateUI();
		}
		public BufferedImage colorToGray(BufferedImage source) {
	        BufferedImage returnValue = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
	        Graphics g = returnValue.getGraphics();
	        g.drawImage(source, 0, 0, null);
	        return returnValue;
	    }
	}
	
	class DetectCornersGUI extends JPanel implements ActionListener {
	    JButton okButton;
	    JButton applyButton;
		JDialog dialog = null;
		JSpinner sigmaSpinner;
		JSpinner thresholdSpinner;
		public DetectCornersGUI() {
			super(new GridLayout(0,1));
	        okButton = new JButton("Ok");
	        okButton.setActionCommand("ok");
	        okButton.addActionListener(this);
	        applyButton = new JButton("Apply");
	        applyButton.setActionCommand("apply");
	        applyButton.addActionListener(this);
	        JPanel rootPanel = new JPanel();
	        GridLayout gridLayout = new GridLayout(2,1, 3, 3);
	        JPanel detectCornersPanel = new JPanel(gridLayout);
	        JLabel sigmaLabel = new JLabel("Sigma: ");
	        double valueSigma = 2.0;
	        double minSigma = 0.0;
	        double maxSigma = 5.0;
	        double stepSigma = 0.1;
	        SpinnerNumberModel sigmaSpinnerNumberModel = new SpinnerNumberModel( valueSigma, minSigma, maxSigma, stepSigma);
	        sigmaSpinner = new JSpinner( sigmaSpinnerNumberModel );
	        detectCornersPanel.add(sigmaLabel);
	        detectCornersPanel.add(sigmaSpinner);
	        JLabel thresholdLabel = new JLabel("Threshold: ");
	        double valueThreshold = 50000.0;
	        double minThreshold = 1.0;
	        double maxThreshold = 100000000.0;
	        double stepThreshold = 1000;
	        SpinnerNumberModel thresholdSpinnerNumberModel = new SpinnerNumberModel( valueThreshold, minThreshold, maxThreshold, stepThreshold );
	        thresholdSpinner = new JSpinner( thresholdSpinnerNumberModel );
	        detectCornersPanel.add(thresholdLabel);
	        detectCornersPanel.add(thresholdSpinner);
			rootPanel.add(detectCornersPanel);
	        JPanel panel = new JPanel();
	        panel.add(applyButton);
	        rootPanel.add(panel);
	        add(rootPanel);
	        GUIValuesToProgram();
		}
		
	    public void createAndShowGUI() {
	        dialog = new JDialog(Assign2UI.this, "Corner Detection", false);
	        dialog.setLocation(new Point(Assign2UI.this.getLocation().x+100, Assign2UI.this.getLocation().y+100));
	        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	        dialog.setModal(true);
	        dialog.setContentPane(this);
	        dialog.pack();
	        dialog.setResizable(false);
	        dialog.setVisible(true);
	    }
	    public void GUIValuesToProgram() {
			double sigma = (Double)sigmaSpinner.getValue();
			double threshold = (Double)thresholdSpinner.getValue();
			byte[] imgData = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();
			corners = new ArrayList<double[]>();
			imgProcessor.obtainCorners(imgData, img.getWidth(), img.getHeight(), sigma, threshold, corners);
			Assign2UI.this.repaint();
	    }
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand() == "apply") {
				GUIValuesToProgram();
				repaint();
			}
		}
	}
}