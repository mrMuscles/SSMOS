package xyz.whoneedspacee.ssmos.attributes;

public class Regeneration extends Attribute {

    protected double regen_amount;
    protected double delay_ticks;

    public Regeneration() {
        this(0.25);
    }

    public Regeneration(double regen_amount) {
        this(regen_amount, 20);
    }

    public Regeneration(double regen_amount, long delay_ticks) {
        super();
        this.name = "Regeneration";
        this.regen_amount = regen_amount;
        this.delay_ticks = delay_ticks;
        task = this.runTaskTimer(plugin, 0, delay_ticks);
    }

    @Override
    public void run() {
        checkAndActivate();
    }

    public void activate() {
        if (!owner.isDead() && owner.getFoodLevel() > 0) {
            owner.setHealth(Math.min(owner.getHealth() + regen_amount, 20));
        }
    }

}
