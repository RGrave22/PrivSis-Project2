import pandas as pd
import matplotlib.pyplot as plt

simple_df = pd.read_csv("bandwidth_simple.csv")
geo_df = pd.read_csv("bandwidth_geo.csv")

data = [simple_df["bandwidth"], geo_df["bandwidth"]]

plt.figure(figsize=(6, 6))

plt.boxplot(
    data,
    tick_labels=["Simple Tor", "Geo-Aware"],
    showmeans=True
)

plt.ylabel("Circuit bandwidth (KB/s)")
plt.title("Circuit bandwidth distribution: Simple Tor vs Geo-Aware")
plt.grid(axis="y", linestyle="--", alpha=0.4)

plt.tight_layout()
plt.savefig("bandwidth_boxplot.png", dpi=300)
# plt.show()
