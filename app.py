"""
app.py
Interactive CLI for the Nova Connector Builder.
Rich terminal UI with progress indicators and formatted output.
"""

import sys
import logging
from pathlib import Path

from dotenv import load_dotenv

load_dotenv()

from rich.console import Console
from rich.panel import Panel
from rich.table import Table
from rich.progress import Progress, SpinnerColumn, TextColumn
from rich.prompt import Prompt, Confirm
from rich.syntax import Syntax
from rich.markdown import Markdown

console = Console()


def show_banner():
    """Display the application banner."""
    banner = """
╔══════════════════════════════════════════════════════════╗
║        🚀 Nova Connector Builder — RAG Agent 🚀         ║
║     ChromaDB + BGE-Large + LLM | Cornerstone on Demand  ║
╚══════════════════════════════════════════════════════════╝
    """
    console.print(banner, style="bold cyan")


def show_stats():
    """Show current knowledge base stats."""
    from vectordb.store import VectorStore

    try:
        store = VectorStore()
        stats = store.get_stats()

        table = Table(title="📊 Knowledge Base Stats", show_header=True)
        table.add_column("Collection", style="cyan")
        table.add_column("Documents", justify="right", style="green")

        for name, count in stats.items():
            table.add_row(name, str(count))

        console.print(table)
    except Exception as e:
        console.print(f"[red]Error loading stats: {e}[/red]")


def run_ingestion():
    """Run the knowledge base ingestion — connector code + framework + golden refs + API docs."""
    console.print("\n[bold yellow]📥 Starting Knowledge Base Ingestion[/bold yellow]")

    from ingest.ingest_connectors import ingest_all
    from ingest.ingest_framework import ingest_framework

    with Progress(
        SpinnerColumn(),
        TextColumn("[progress.description]{task.description}"),
        console=console,
    ) as progress:
        # Phase 1: Framework — golden refs, base classes, rules
        task1 = progress.add_task("Ingesting framework knowledge & golden references...", total=None)
        try:
            ingest_framework(reset=True)
        except Exception as e:
            console.print(f"[yellow]Warning: Framework ingestion: {e}[/yellow]")
        progress.update(task1, description="✅ Framework & golden references ingested")

        # Phase 2: All connector source code
        task2 = progress.add_task("Ingesting connector code...", total=None)
        stats = ingest_all(reset=True)
        progress.update(task2, description="✅ Connector code ingested")

        # Phase 3: Nova docs (legacy, graceful fail)
        task3 = progress.add_task("Ingesting Nova docs...", total=None)
        try:
            from ingest.ingest_api_docs import ingest_nova_docs
            ingest_nova_docs()
        except Exception as e:
            console.print(f"[dim]Nova docs skipped: {e}[/dim]")
        progress.update(task3, description="✅ Nova docs processed")

    show_stats()


def run_generation(prompt: str):
    """Run the connector generation pipeline."""
    from pipeline import run_pipeline

    console.print(f"\n[bold green]🔨 Generating connector...[/bold green]")
    console.print(f"[dim]Prompt: {prompt}[/dim]\n")

    result = run_pipeline(prompt=prompt)

    # Display results
    console.print(f"\n[bold]{'='*60}[/bold]")
    console.print(
        Panel(
            f"[bold green]Connector: {result['connector_name']}[/bold green]\n"
            f"Auth: {result['spec']['auth_type']}\n"
            f"Entities: {', '.join(result['spec']['entity_types'])}\n"
            f"Direction: {result['spec']['sync_direction']}\n"
            f"Output: {result['output_dir']}",
            title="📦 Generation Result",
        )
    )

    # File table
    table = Table(title="Generated Files", show_header=True)
    table.add_column("File", style="cyan")
    table.add_column("Type", style="blue")
    table.add_column("Size", justify="right")
    table.add_column("Status", justify="center")

    for f in result["files"]:
        size = len(f["content"])
        status = "✅" if "FAIL" not in str(f.get("validation", "")) else "❌"
        table.add_row(f["file_name"], f["file_type"], f"{size:,} chars", status)

    console.print(table)

    # Validation
    console.print(f"\n[bold]Validation: {'✅ ALL PASSED' if result['validation_passed'] else '❌ SOME FAILED'}[/bold]")
    for vr in result["validation_results"]:
        console.print(f"  {vr}")

    # Ask to view files
    if Confirm.ask("\nView generated files?", default=False):
        for f in result["files"]:
            lang = {"java": "java", "javascript": "javascript", "xml": "xml", "sql": "sql"}.get(
                f["file_type"], "text"
            )
            if f["file_name"].endswith(".java"):
                lang = "java"
            elif f["file_name"].endswith(".js"):
                lang = "javascript"
            elif f["file_name"].endswith(".xml"):
                lang = "xml"
            elif f["file_name"].endswith(".sql"):
                lang = "sql"

            console.print(f"\n[bold cyan]── {f['file_name']} ──[/bold cyan]")
            syntax = Syntax(
                f["content"][:2000],
                lang,
                theme="monokai",
                line_numbers=True,
            )
            console.print(syntax)
            if len(f["content"]) > 2000:
                console.print(f"[dim]... ({len(f['content']) - 2000} more characters)[/dim]")

    return result


def interactive_mode():
    """Run the interactive CLI loop."""
    show_banner()
    show_stats()

    while True:
        console.print("\n[bold]Commands:[/bold]")
        console.print("  [cyan]generate[/cyan]  — Generate a new connector")
        console.print("  [cyan]ingest[/cyan]    — Re-ingest knowledge base")
        console.print("  [cyan]stats[/cyan]     — Show knowledge base stats")
        console.print("  [cyan]quit[/cyan]      — Exit")

        command = Prompt.ask("\n[bold]>>[/bold]", default="generate").strip().lower()

        if command in ("quit", "exit", "q"):
            console.print("[dim]Goodbye! 👋[/dim]")
            break

        elif command == "stats":
            show_stats()

        elif command == "ingest":
            if Confirm.ask("This will reset and re-ingest all data. Continue?"):
                run_ingestion()

        elif command == "generate":
            prompt = Prompt.ask(
                "[bold]Enter your connector request[/bold]",
                default="Create a Stripe connector with OAuth2 for content sync",
            )
            run_generation(prompt)

        else:
            # Treat unknown input as a generation prompt
            run_generation(command)


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s [%(levelname)s] %(message)s",
    )

    if len(sys.argv) > 1:
        # Non-interactive: treat args as prompt
        prompt = " ".join(sys.argv[1:])
        run_generation(prompt)
    else:
        interactive_mode()
