package Main;

public class Selection
{
	private boolean button;
	private int buttonOneX, buttonOneY, buttonTwoX, buttonTwoY;
	private int switchX, switchY;
	private int buttonOneType, buttonTwoType, switchType;
	private int laserX, laserY;
	private int laserSize;

	public Selection()
	{
		button = false;
		laserSize = 0;
	}

	public void selectFirstButton(int x, int y, int type)
	{
		button = true;
		buttonOneX = x;
		buttonOneY = y;
		buttonOneType = type;
	}

	public void selectSecondButton(int x, int y, int type)
	{
		buttonTwoX = x;
		buttonTwoY = y;
		buttonTwoType = type;
	}

	public void selectSwitch(int x, int y, int type)
	{
		switchX = x;
		switchY = y;
		switchType = type;
	}

	public void selectLaser(int x, int y)
	{
		laserSize++;
		laserX = x;
		laserY = y;
	}

	public String toString()
	{
		if (button)
		{
			return "Button " + buttonOneType + " " + buttonTwoType + " " + 13
					+ " " + 1 + " " + buttonOneX + " " + buttonOneY + " "
					+ buttonTwoX + " " + buttonTwoY + " " + laserX + " "
					+ laserY;
		}
		// Switch
		else
		{
			return "Switch " + switchType + " " + 13 + " " + laserSize
					+ " " + switchX + " " + switchY + " " + laserX + " "
					+ laserY;
		}
	}
}
