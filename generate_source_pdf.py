import os
import html

extensions = [".kt", ".xml", ".kts", ".toml", ".json"]
exclude_dirs = [".gradle", "build", ".idea", ".git", "gradle"]

files_data = []
for root, dirs, files in os.walk("."):
    dirs[:] = [d for d in dirs if d not in exclude_dirs]
    for file in files:
        if any(file.endswith(ext) for ext in extensions):
            path = os.path.join(root, file)
            # normalize path
            path = path.replace("\\", "/")
            if path.startswith("./"):
                path = path[2:]
            try:
                with open(path, "r", encoding="utf-8") as f:
                    content = f.read()
                files_data.append({"path": path, "content": content})
            except Exception as e:
                pass

# Sort files by path
files_data.sort(key=lambda x: x["path"])

html_content = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Soft Keeper - Full Source Code Documentation</title>
    <style>
        body {{
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
            line-height: 1.6;
            color: #1e293b;
            background: #f8fafc;
            margin: 0;
            padding: 40px;
        }}
        .container {{
            max-width: 900px;
            margin: 0 auto;
            background: #ffffff;
            padding: 50px;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.08);
        }}
        h1 {{
            color: #0f172a;
            font-size: 32px;
            border-bottom: 3px solid #064e3b;
            padding-bottom: 15px;
            margin-bottom: 10px;
        }}
        .subtitle {{
            color: #64748b;
            font-size: 16px;
            margin-bottom: 30px;
        }}
        .toc {{
            background: #f1f5f9;
            padding: 20px 30px;
            border-radius: 8px;
            margin-bottom: 40px;
        }}
        .toc h2 {{
            margin-top: 0;
            color: #0f172a;
            font-size: 20px;
        }}
        .toc ul {{
            margin: 0;
            padding-left: 20px;
        }}
        .toc li {{
            margin-bottom: 6px;
        }}
        .toc a {{
            color: #0284c7;
            text-decoration: none;
            font-weight: 500;
        }}
        .toc a:hover {{
            text-decoration: underline;
        }}
        .file-section {{
            margin-bottom: 50px;
            page-break-inside: avoid;
        }}
        .file-header {{
            background: #0f172a;
            color: #ffffff;
            padding: 12px 20px;
            font-family: monospace;
            font-size: 15px;
            font-weight: bold;
            border-top-left-radius: 8px;
            border-top-right-radius: 8px;
            display: flex;
            justify-content: space-between;
        }}
        pre {{
            background: #0b0f19;
            color: #e2e8f0;
            padding: 20px;
            margin: 0;
            border-bottom-left-radius: 8px;
            border-bottom-right-radius: 8px;
            overflow-x: auto;
            font-family: 'Fira Code', Consolas, Monaco, 'Courier New', monospace;
            font-size: 13px;
            line-height: 1.5;
        }}
        @media print {{
            body {{
                background: #ffffff;
                padding: 0;
            }}
            .container {{
                box-shadow: none;
                padding: 0;
                max-width: 100%;
            }}
            .file-section {{
                page-break-inside: avoid;
                margin-bottom: 30px;
            }}
            pre {{
                white-space: pre-wrap;
                word-wrap: break-word;
            }}
        }}
    </style>
</head>
<body>
    <div class="container">
        <h1>Soft Keeper Android App</h1>
        <div class="subtitle">Complete Source Code & Technical Documentation (Generated automatically)</div>

        <div class="toc">
            <h2>Table of Contents ({len(files_data)} files)</h2>
            <ul>
"""

for idx, f in enumerate(files_data):
    anchor = f"file-{idx}"
    html_content += f'                <li><a href="#{anchor}">{html.escape(f["path"])}</a></li>\n'

html_content += """            </ul>
        </div>
"""

for idx, f in enumerate(files_data):
    anchor = f"file-{idx}"
    escaped_code = html.escape(f["content"])
    html_content += f"""
        <div class="file-section" id="{anchor}">
            <div class="file-header">
                <span>{html.escape(f["path"])}</span>
                <span>{len(f["content"].splitlines())} lines</span>
            </div>
            <pre><code>{escaped_code}</code></pre>
        </div>
"""

html_content += """
    </div>
</body>
</html>
"""

with open("source_code_report.html", "w", encoding="utf-8") as out:
    out.write(html_content)

print(f"Generated source_code_report.html successfully with {len(files_data)} files.")
