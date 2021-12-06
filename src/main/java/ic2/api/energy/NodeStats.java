package ic2.api.energy;

public class NodeStats {
    protected double energyIn;

    public NodeStats(double energyIn, double energyOut, double voltage) {
        this.energyIn = energyIn;
        this.energyOut = energyOut;
        this.voltage = voltage;
    }
    protected double energyOut; protected double voltage;

    public double getEnergyIn() { return this.energyIn; }



    public double getEnergyOut() { return this.energyOut; }



    public double getVoltage() { return this.voltage; }
}
