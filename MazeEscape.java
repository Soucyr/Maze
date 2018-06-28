    import javax.swing.JFrame;
    import asciiPanel.AsciiPanel;
    import java.awt.event.KeyEvent;
    import java.awt.event.KeyListener;
    import java.util.*;
    import java.awt.Color;

    public class EscapeTheMonsters extends JFrame implements KeyListener {

        public static final int WIDTH = 39;
        public static final int HEIGHT = 27;
        public static final double LOS_RADIUS = 6.0;
        public static final int Weapon_LENGTH = 8;
        public static final Color FLOOR_COLOR = new Color(30, 30, 30);
        
        private AsciiPanel terminal;
        private String maze =
            "#######################################\n" +
            "#######################################\n" +
            "## #       #       #     #         # ##\n" +
            "## # ##### # ### ##### ### ### ### # ##\n" +
            "##       #   # #     #     # # #   # ##\n" +
            "###### # ##### ##### ### # # # ##### ##\n" +
            "##   # #       #     # # # # #     # ##\n" +
            "## # ####### # # ##### ### # ##### # ##\n" +
            "## #       # # #   #     #     #   # ##\n" +
            "## ####### ### ### # ### ##### # ### ##\n" +
            "##     #   # #   # #   #     # #     ##\n" +
            "## ### ### # ### # ##### # # # ########\n" +
            "##   #   # # #   #   #   # # #   #   ##\n" +
            "######## # # # ##### # ### # ### ### ##\n" +
            "##     # #     #   # #   # #   #     ##\n" +
            "## ### # ##### ### # ### ### ####### ##\n" +
            "## #   #     #     #   # # #       # ##\n" +
            "## # ##### # ### ##### # # ####### # ##\n" +
            "## #     # # # # #     #       # #   ##\n" +
            "## ##### # # # ### ##### ##### # ######\n" +
            "## #   # # #     #     # #   #       ##\n" +
            "## # ### ### ### ##### ### # ##### # ##\n" +
            "## #         #     #       #       # ##\n" +
            "##X####################################\n" +
            "#######################################\n";
            
            private Random rng = new Random();
            private char[][] map;
            private List<Monster> Monsters = new ArrayList<>();
            int x;
            int y;
            private boolean won = false;
            private boolean lost = false;
            String statusMsg = "";
            private boolean hasWeapon = false;
            private int Weapon = 0; //number of turns to render Weapon
            private int WeaponAmmo = 0;
           

        public EscapeTheMonsters() {//setup the game
            super("Escape The Monsters");
            terminal = new AsciiPanel(WIDTH, HEIGHT);
            map = loadMap();
            placePlayerRandomly();
            placeBeamWeapon();
            addMonsters(10);
            add(terminal);
            pack();
            addKeyListener(this);
            repaint();
        }
        
        public void placePlayerRandomly() {
            while (true) {
                x = rng.nextInt(WIDTH);
                y = rng.nextInt(HEIGHT);
                if (map[y][x] == ' ')
                    break;
            }
            map[y][x] = '>';
        }
        
        public void placeBeamWeapon() {//randomly placed
            int c = 0;
            int r = 0;
            while (true) {
                c = rng.nextInt(WIDTH);
                r = rng.nextInt(HEIGHT);
                if (map[r][c] == ' ')
                    break;
            }
            map[r][c] = 'L';
        }
        
        //n = how many to add
        public void addMonsters(int n) {
            for (int i = 0; i < n; i++) {
                int tx = 0;
                int ty = 0;
                while (true) {
                    tx = rng.nextInt(WIDTH);
                    ty = rng.nextInt(HEIGHT);
                    if (map[ty][tx] == ' ')
                        break;
                }
                map[ty][tx] = 'T';
                Monsters.add(new Monster(tx, ty));
            }
        }
        
        public void repaint() {
            terminal.clear();
            displayOutput(terminal);
            super.repaint();
        }
        
        public void movePlayer(int x, int y) {//movement and status updates
            char ch = map[this.y + y][this.x + x];
            if (ch == ' ' || ch == 'L' || ch == '*') {
                
                this.x = this.x + x;//position update
                this.y = this.y + y;
          
                }
                else {
                    char g = gibs == 0 ? ' ' : '.';
                    map[this.y - y][this.x - x] = g;
                    if (gibs != 0)
                        gibs--;
                }
                if (ch == 'L') {
                    hasWeapon = true;
                    statusMsg = "You pick up a Weapon!";
                    WeaponAmmo = 2;
                }
            }
            else if (ch == '#')
                pushWall(this.x + x, this.y + y, x, y);
            else if (ch == 'T')
                lost = true;
            else if (ch == 'X') {
                won = true;
                System.out.println("WON");
            }
            char c = '@';
            if (x == 1) c = '>';
            else if (x == -1) c = '<';
            else if (y == 1) c = 'v';
            else if (y == -1) c = '^';
            map[this.y][this.x] = c;
        }
        
        //from (c, r) toward (x, y)
        public void pushWall(int c, int r, int x, int y) {//moveable walls, like in the old mouse and cat game
            char ch = map[r + y][c + x];
            if (ch == ' ') {
                statusMsg = "Rumble";
                map[r][c] = ' ';
                
            }
            else if (ch == 'T') {
                map[r][c] = ' ';
                killMonster(c + x, r + y);
            }
        }
        
        public boolean inBounds(int x, int y) {
            return x > 1 && x < WIDTH - 2 && y > 1 && y < HEIGHT - 4;
        }
        
        //kill the Monster at x, y
        public void killMonster(int x, int y) {
            statusMsg = "You killed a monster!";
            Monster t = null;
            for (Monster Monster : Monsters)
                if (Monster.tx == x && Monster.ty == y)
                    t = Monster;
            Monsters.remove(t);
        }
        
        public void displayOutput(AsciiPanel term) {
            if (Weapon > 0) {
                Weapon--;
                if (Weapon == 0) {
                    for (int r = 0; r < HEIGHT; r++)
                        for (int c = 0; c < WIDTH; c++)
                            if (map[r][c] == '*')
                                map[r][c] = ' ';
                }
            }
            Color color = Color.WHITE;
            for (int r = 0; r < HEIGHT - 2; r++)
                for (int c = 0; c < WIDTH; c++) {
                    char ch = map[r][c];
                    if (player(ch))
                        color = Color.YELLOW;
                    else if (ch == 'X')
                        color = Color.MAGENTA;
                    else if (ch == 'T') {
                        for (Monster t : Monsters) {
                            if (t.tx == c && t.ty == r)
                                if (t.speed > 0.0D)
                                    color = Color.ORANGE;
                                else
                                    color = Color.RED;
                        }
                    }
                    
                    else if (ch == 'L' || ch == '*')
                        color = Color.CYAN;
                    else
                        color = Color.GRAY;
                    boolean visable = distance(c, r) < LOS_RADIUS;
                    if (visable) {
                        if (ch == '#')
                            term.write(map[r][c], c, r, color, Color.DARK_GRAY);
                        else 
                            term.write(map[r][c], c, r, color, FLOOR_COLOR);
                    }
                }
            displayStatusMsg();
            term.write("Weapon Ammo: " + WeaponAmmo, 0, 26, Color.CYAN);
            if (won) {
                term.writeCenter("You've escaped the maze", 10, Color.GREEN);
                term.writeCenter("Press [Esc]to exit.", 12, Color.GREEN);
            }
            else if (lost) {
                term.writeCenter("You were eaten by a Monster...", 10, Color.GREEN);
                term.writeCenter("Press --[Esc]-- to exit.", 12, Color.GREEN);
            }
        }
        
        public void displayStatusMsg() {
            terminal.write(statusMsg, 0, 25);
        }
        
        public double distance(int x, int y) {
            return Math.sqrt(Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2));
        }
        
        public boolean player(char c) {//Player icon and direction
            return c == '>' || c == '<' || c == '^' || c == 'v';
        }
        
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP: if (!won && !lost) movePlayer(0, -1); break;                
                case KeyEvent.VK_DOWN: if (!won && !lost) movePlayer(0, 1); break;
                case KeyEvent.VK_LEFT: if (!won && !lost) movePlayer(-1, 0); break;
                case KeyEvent.VK_RIGHT: if (!won && !lost) movePlayer(1, 0); break;
                case KeyEvent.VK_SPACE:
                    if (!won && !lost && hasWeapon && WeaponAmmo > 0) {
                        int dx = 0;
                        int dy = 0;
                        Weapon = 2;
                        switch (map[y][x]) {
                            case '>': dx = 1;
                            break;
                            case '<': dx = -1;
                            break;
                            case '^': dy = -1;
                            break;
                            case 'v': dy = 1;
                            break;
                        }
                        if (dy == 0) {
                            for (int c = x + dx; c != x + Weapon_LENGTH * dx; c += dx) {
                                if (inBounds(c, y)) {
                                    if (map[y][c] == 'T') {
                                        killMonster(c, y);
                                        statusMsg = "The Monster is killed.";
                                    }
                                    map[y][c] = '*';
                                }
                            }
                        }
                        else if (dx == 0) {
                            for (int r = y + dy; r != y + Weapon_LENGTH * dy; r += dy) {
                                if (inBounds(x, r)) {
                                    if (map[r][x] == 'T') {
                                        killMonster(x, r);
                                        statusMsg = "The Monster is killed.";
                                    }
                                    map[r][x] = '*';
                                }
                            }
                        }
                        WeaponAmmo -= 1;
                    }
                break;
                case KeyEvent.VK_ESCAPE: System.exit(0); break;
            }
            if (!won && !lost)
                for (Monster t : Monsters) {
                    t.move();
                    if (Math.random() < t.speed)
                        t.move();
                }
            repaint();
        }
        
        public void keyReleased(KeyEvent e) {}
        
        public void keyTyped(KeyEvent e) {}
        
        public char[][] loadMap() {
            char[][] map = new char[HEIGHT][WIDTH];
            int r = 0;
            int c = 0;
            for (char ch : maze.toCharArray()) {
                if (c >= WIDTH)
                    c = 0;
                if (r >= HEIGHT)
                    break;
                else if (ch == '\n')
                    r++;
                else {
                    map[r][c] = ch;
                    c++;
                }
            }
            return map;
        }

        public static void main(String[] args) {
            EscapeTheMonsters window = new EscapeTheMonsters();
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.setLocationRelativeTo(null);
            window.setVisible(true);
        }
        
        private class Monster {
            
            int tx;
            int ty;
            double speed = 0.0D;
            
            Monster(int x, int y) {
                tx = x;
                ty = y;
            }
            
            
            boolean close() {//Determine proximity to plaer
                return distance(tx, ty) < 6.0;
            }
            
            boolean canSeePlayer() {
                boolean canSee = false;
                if (x == tx) {
                    int dir = y > ty ? 1 : -1;
                    for (int r = ty + dir; r != y; r += dir) {
                        if (map[r][x] == '#') {
                            return false;
                        }
                    }
                    return true;
                }
                else if (y == ty) {
                    int dir = x > tx ? 1 : -1;
                    for (int c = tx + dir; c != x; c += dir) {
                        if (map[y][c] == '#') {
                            return false;
                        }
                    } 
                    return true;
                }
                return canSee;
            }
            
            void shuffle() {//Monster wanders when not nearby
                int dx = 0;
                int dy = 0;
                int randomDir = Math.random() > 0.5 ? 1 : -1;
                if (rng.nextInt(100) > 50)
                    dx = randomDir;
                else
                    dy = randomDir;
                char ch = map[ty + dy][tx + dx];
                if (ch == ' ') {
                    if (ch == '%'){ 
                        speed += 0.1;
                    }
                    map[ty][tx] = ' ';
                    map[ty + dy][tx + dx] = 'T';
                    tx = tx + dx;
                    ty = ty + dy;
                }
                else if (player(ch))
                    lost = true;
            }
            
            
            void chase() {//Monster sees the player and chaeses them
                if (x == tx) {
                    int dir = y > ty ? 1 : -1;
                    char ch = map[ty + dir][tx];
                    if (player(ch))
                        lost = true;
                    else if (ch != '#') {//movement
                        map[ty][tx] = ' ';
                        map[ty + dir][tx] = 'T';
                        ty = ty + dir;
                    }
                }
                else if (y == ty) {
                    int dir = x > tx ? 1 : -1;
                    char ch = map[ty][tx + dir];
                    if (player(ch))
                        lost = true;
                    else if (ch != '#') {
                        map[ty][tx] = ' ';
                        map[ty][tx + dir] = 'T';
                        tx = tx + dir;
                    }
                }
                else
                    shuffle();
            }
            
            void move() {
                if (close() && canSeePlayer()) {
                    statusMsg = "Uh-oh! You've been spotted! Run!";
                    chase();
                }
                else
                    shuffle();
            }
        }
    }