import pandas as pd
import matplotlib.pyplot as plt


simple_df = pd.read_csv("bandwidth_simple.csv")
geo_df = pd.read_csv("bandwidth_geo.csv")


simple_df = simple_df.sort_values("circuit_id")
geo_df = geo_df.sort_values("circuit_id")

plt.figure(figsize=(10, 6))


plt.plot(simple_df["circuit_id"], simple_df["bandwidth"],
         label="Simple Tor", color="tab:blue", alpha=0.8)
plt.plot(geo_df["circuit_id"], geo_df["bandwidth"],
         label="Geo-Aware", color="tab:orange", alpha=0.8)

plt.xlabel("Circuit ID")
plt.ylabel("Circuit bandwidth (KB/s)")
plt.title("Circuit bandwidth per circuit: Simple Tor vs Geo-Aware")
plt.legend()
plt.grid(True, linestyle="--", alpha=0.4)

plt.tight_layout()
plt.savefig("bandwidth_comparison.png", dpi=300)
# plt.show()  # opcional, se quiseres ver o gráfico na janela
