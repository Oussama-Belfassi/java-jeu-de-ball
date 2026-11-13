package ball;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/** Panneau du jeu : balle, raquette, briques, score et vies. */
public class GamePanel extends JPanel implements ActionListener {

    static final int W = 700, H = 550;
    private static final int ROWS = 5, COLS = 10;
    private static final int BRICK_W = 60, BRICK_H = 22, GAP = 6;

    private final Timer timer = new Timer(10, this);

    private double ballX, ballY, vx, vy;
    private final int ballR = 9;
    private double paddleX;
    private final int paddleW = 100, paddleH = 14;
    private boolean left, right;

    private final List<Rectangle> bricks = new ArrayList<>();
    private final List<Color> brickColors = new ArrayList<>();

    private int score, lives, level;
    private boolean running, paused, gameOver, won;

    public GamePanel() {
        setPreferredSize(new Dimension(W, H));
        setBackground(new Color(18, 20, 33));
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT: case KeyEvent.VK_A: left = true; break;
                    case KeyEvent.VK_RIGHT: case KeyEvent.VK_D: right = true; break;
                    case KeyEvent.VK_SPACE:
                        if (gameOver || won) newGame();
                        else if (!running) launch();
                        else paused = !paused;
                        break;
                    case KeyEvent.VK_R: newGame(); break;
                    default:
                }
            }
            @Override public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT: case KeyEvent.VK_A: left = false; break;
                    case KeyEvent.VK_RIGHT: case KeyEvent.VK_D: right = false; break;
                    default:
                }
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) { movePaddleTo(e.getX()); }
            @Override public void mouseDragged(MouseEvent e) { movePaddleTo(e.getX()); }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                if (gameOver || won) newGame();
                else if (!running) launch();
            }
        });

        newGame();
        timer.start();
    }

    private void movePaddleTo(int mx) {
        paddleX = Math.max(0, Math.min(W - paddleW, mx - paddleW / 2.0));
        if (!running) resetBall();
    }

    private void newGame() {
        score = 0;
        lives = 3;
        level = 1;
        gameOver = false;
        won = false;
        buildBricks();
        resetBall();
    }

    private void buildBricks() {
        bricks.clear();
        brickColors.clear();
        Color[] rowColors = {new Color(239, 83, 80), new Color(255, 167, 38), new Color(255, 238, 88),
                new Color(102, 187, 106), new Color(66, 165, 245)};
        int totalW = COLS * BRICK_W + (COLS - 1) * GAP;
        int offX = (W - totalW) / 2;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                bricks.add(new Rectangle(offX + c * (BRICK_W + GAP), 60 + r * (BRICK_H + GAP), BRICK_W, BRICK_H));
                brickColors.add(rowColors[r]);
            }
        }
    }

    private void resetBall() {
        running = false;
        paused = false;
        ballX = paddleX + paddleW / 2.0;
        ballY = H - 50 - ballR;
        vx = 0;
        vy = 0;
    }

    private void launch() {
        running = true;
        double speed = 3.2 + (level - 1) * 0.5;
        vx = speed * Math.sin(Math.toRadians(-30 + Math.random() * 60));
        vy = -Math.sqrt(speed * speed - vx * vx);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!paused && !gameOver && !won) update();
        repaint();
    }

    private void update() {
        if (left) paddleX = Math.max(0, paddleX - 6);
        if (right) paddleX = Math.min(W - paddleW, paddleX + 6);

        if (!running) {
            ballX = paddleX + paddleW / 2.0;
            return;
        }

        ballX += vx;
        ballY += vy;

        // murs
        if (ballX - ballR < 0) { ballX = ballR; vx = -vx; }
        if (ballX + ballR > W) { ballX = W - ballR; vx = -vx; }
        if (ballY - ballR < 0) { ballY = ballR; vy = -vy; }

        // raquette
        Rectangle paddle = new Rectangle((int) paddleX, H - 40, paddleW, paddleH);
        if (vy > 0 && ballY + ballR >= paddle.y && ballY + ballR <= paddle.y + paddleH + 6
                && ballX >= paddle.x - ballR && ballX <= paddle.x + paddleW + ballR) {
            double hit = (ballX - (paddle.x + paddleW / 2.0)) / (paddleW / 2.0); // -1..1
            double speed = Math.hypot(vx, vy);
            double ang = hit * Math.toRadians(60);
            vx = speed * Math.sin(ang);
            vy = -speed * Math.cos(ang);
            ballY = paddle.y - ballR;
        }

        // briques
        for (int i = 0; i < bricks.size(); i++) {
            Rectangle b = bricks.get(i);
            if (circleHitsRect(ballX, ballY, ballR, b)) {
                double overlapL = ballX + ballR - b.x;
                double overlapR = b.x + b.width - (ballX - ballR);
                double overlapT = ballY + ballR - b.y;
                double overlapB = b.y + b.height - (ballY - ballR);
                double min = Math.min(Math.min(overlapL, overlapR), Math.min(overlapT, overlapB));
                if (min == overlapL || min == overlapR) vx = -vx; else vy = -vy;
                bricks.remove(i);
                brickColors.remove(i);
                score += 10;
                break;
            }
        }

        if (bricks.isEmpty()) {
            if (level >= 3) {
                won = true;
                running = false;
            } else {
                level++;
                buildBricks();
                resetBall();
            }
        }

        // balle perdue
        if (ballY - ballR > H) {
            lives--;
            if (lives <= 0) {
                gameOver = true;
                running = false;
            } else {
                resetBall();
            }
        }
    }

    private static boolean circleHitsRect(double cx, double cy, double r, Rectangle rect) {
        double nx = Math.max(rect.x, Math.min(cx, rect.x + rect.width));
        double ny = Math.max(rect.y, Math.min(cy, rect.y + rect.height));
        double dx = cx - nx, dy = cy - ny;
        return dx * dx + dy * dy <= r * r;
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // briques
        for (int i = 0; i < bricks.size(); i++) {
            Rectangle b = bricks.get(i);
            g.setColor(brickColors.get(i));
            g.fillRoundRect(b.x, b.y, b.width, b.height, 8, 8);
        }

        // raquette
        g.setColor(new Color(236, 239, 241));
        g.fillRoundRect((int) paddleX, H - 40, paddleW, paddleH, 10, 10);

        // balle
        g.setColor(new Color(255, 213, 79));
        g.fillOval((int) (ballX - ballR), (int) (ballY - ballR), ballR * 2, ballR * 2);

        // HUD
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.drawString("Score : " + score, 15, 28);
        g.drawString("Niveau : " + level + "/3", W / 2 - 40, 28);
        g.drawString("Vies : " + lives, W - 90, 28);

        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        if (gameOver) center(g, "GAME OVER - Score " + score, H / 2, "Espace ou clic pour rejouer");
        else if (won) center(g, "BRAVO ! Score " + score, H / 2, "Espace ou clic pour rejouer");
        else if (paused) center(g, "PAUSE", H / 2, "Espace pour reprendre");
        else if (!running) center(g, "Prêt ?", H / 2 + 60, "Clic ou Espace pour lancer - souris ou ← → pour bouger");
    }

    private void center(Graphics2D g, String title, int y, String sub) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (W - fm.stringWidth(title)) / 2, y);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        fm = g.getFontMetrics();
        g.drawString(sub, (W - fm.stringWidth(sub)) / 2, y + 28);
    }
}
