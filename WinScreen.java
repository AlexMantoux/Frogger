import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WinScreen extends JFrame {
    public WinScreen() {
        setTitle("CONGRATS 🎉 YOU WON");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(0, 0, 255);
                Color color2 = Color.WHITE;
                GradientPaint gradient = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new BorderLayout());

        JLabel label = new JLabel("YOU WON", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 60));
        label.setForeground(new Color(255, 223, 0));
        label.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
        panel.add(label, BorderLayout.CENTER);

        JButton restartButton = new JButton("Restart");
        restartButton.setFont(new Font("Arial", Font.BOLD, 24));
        restartButton.setForeground(Color.WHITE);
        restartButton.setBackground(new Color(34, 139, 34));
        restartButton.setFocusPainted(false);
        restartButton.setBorderPainted(false);
        restartButton.setPreferredSize(new Dimension(200, 60));
        restartButton.setOpaque(true);
        restartButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        restartButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                restartButton.setBackground(new Color(50, 205, 50));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                restartButton.setBackground(new Color(34, 139, 34));
            }
        });

        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                restartGame();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        buttonPanel.add(restartButton, gbc);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }

    private void restartGame() {
        FroggerGame.main(new String[]{});
    }

    public static void main(String[] args) {
        new WinScreen();
    }
}