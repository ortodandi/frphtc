#!/usr/bin/env python3
"""Simple CLI for analyzing pcap files for suspicious mobile network activity."""

import argparse
from collections import Counter
from pathlib import Path

try:
    import pyshark
except ImportError:  # pragma: no cover
    pyshark = None

def load_indicators(path: Path) -> set:
    """Load indicators (domains or IPs) from a file."""
    if not path:
        return set()
    indicators = set()
    with open(path, "r", encoding="utf-8") as handle:
        for line in handle:
            line = line.strip()
            if line and not line.startswith("#"):
                indicators.add(line)
    return indicators

def analyze_pcap(pcap_path: Path, domains: set, ips: set):
    """Analyze a pcap file and return summary information."""
    if pyshark is None:
        raise SystemExit("pyshark is required. Install with 'pip install pyshark'.")

    suspicious = []
    cell_ids = Counter()

    capture = pyshark.FileCapture(str(pcap_path), keep_packets=False)
    for pkt in capture:
        try:
            if hasattr(pkt, "http"):
                host = getattr(pkt.http, "host", "")
                if host in domains:
                    suspicious.append((pkt.number, f"HTTP host {host}"))
            if hasattr(pkt, "dns"):
                query = getattr(pkt.dns, "qry_name", "")
                if query in domains:
                    suspicious.append((pkt.number, f"DNS query {query}"))
            if hasattr(pkt, "ip"):
                if pkt.ip.src in ips or pkt.ip.dst in ips:
                    suspicious.append((pkt.number, f"IP communication with {pkt.ip.src}/{pkt.ip.dst}"))
            # Mobile network identifiers
            if hasattr(pkt, "lte_rrc"):
                cid = getattr(pkt.lte_rrc, "cell_id", None)
                if cid:
                    cell_ids[cid] += 1
            if hasattr(pkt, "gsm_a_rr"):
                cid = getattr(pkt.gsm_a_rr, "cell_identity", None)
                if cid:
                    cell_ids[cid] += 1
        except Exception:
            continue
    capture.close()
    return suspicious, cell_ids

def main() -> None:
    parser = argparse.ArgumentParser(description="Analyze pcap for spyware, fake towers, and interventions")
    parser.add_argument("pcap", type=Path, help="Path to the pcap file")
    parser.add_argument("--domains", type=Path, help="File with suspicious domain names")
    parser.add_argument("--ips", type=Path, help="File with suspicious IP addresses")
    args = parser.parse_args()

    domains = load_indicators(args.domains)
    ips = load_indicators(args.ips)

    suspicious, cell_ids = analyze_pcap(args.pcap, domains, ips)

    print(f"Unique cell IDs observed: {len(cell_ids)}")
    if len(cell_ids) > 1:
        print("WARNING: multiple cell towers detected; possible rogue base station.")

    if suspicious:
        print("Suspicious activity detected:")
        for num, info in suspicious:
            print(f"  Packet {num}: {info}")
    else:
        print("No suspicious activity found with current indicators.")

if __name__ == "__main__":  # pragma: no cover
    main()
