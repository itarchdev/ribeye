package ru.it_arch.tools.samples.ribeye.dsl

import ru.it_arch.k3dm.ValueObject

public interface CookingProcess : ValueObject.Data {
    public val `get meat from storage`: Op.Meat.Get
    public val `check meat freshness`: Op.Meat.Check
    public val `marinate meat`: Op.Meat.Marinate
    public val `get grill from storage`: Op.Grill.Get
    public val `check grill`: Op.Grill.Check
    public val `get sauce ingredients from storage`: Op.Sauce.Get
    public val `prepare sauce`: Op.Sauce.Prepare
    public val `get rosemary from storage`: Op.Rosemary.Get
    public val `roast rosemary`: Op.Rosemary.Roast
    public val `put meat on the grill and start roasting`: Op.Meat.PrepareForRoasting
    public val `roast meat`: Op.Meat.Roast
    public val `serve steak`: Op.Meat.Serve
    public val `final check and create ribeye steak`: Op.Finish

    override fun validate() {}
}
