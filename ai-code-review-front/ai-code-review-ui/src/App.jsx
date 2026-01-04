import { useMemo, useState } from "react";
import { downloadUpdated, uploadApply } from "./api";
import "./App.css";

function IconSparkle() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" className="icon">
      <path
        d="M12 2l1.2 6.2L20 10l-6.8 1.8L12 18l-1.2-6.2L4 10l6.8-1.8L12 2z"
        fill="currentColor"
        opacity="0.9"
      />
      <path
        d="M19 14l.7 3.3L23 18l-3.3.7L19 22l-.7-3.3L15 18l3.3-.7L19 14z"
        fill="currentColor"
        opacity="0.55"
      />
    </svg>
  );
}

function CodeBlock({ text }) {
  return (
    <pre className="code">
      <code>{text}</code>
    </pre>
  );
}

function MethodCard({ m, index }) {
  return (
    <div className="card" style={{ animationDelay: `${index * 40}ms` }}>
      <div className="cardTop">
        <div className="methodTitle">{m.methodName}</div>
        <div className="chip">Suggested: {m.suggestedMethodName || "-"}</div>
      </div>

      <div className="label">Review</div>
      <ul className="bullets">
        {(m.reviewComments || []).map((c, idx) => (
          <li key={idx}>{c}</li>
        ))}
      </ul>

      <div className="label">Suggested Javadoc</div>
      <CodeBlock text={m.suggestedJavadoc || ""} />
    </div>
  );
}

export default function App() {
  const [file, setFile] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [data, setData] = useState(null);
  const [tab, setTab] = useState("review");

  const filename = useMemo(() => file?.name ?? "No file selected", [file]);

  async function handleReviewApply() {
    setError("");
    setData(null);
    if (!file) return setError("Pick a .java file first.");

    setBusy(true);
    try {
      const json = await uploadApply(file);
      setData(json);
      setTab("review");
    } catch (e) {
      setError(e?.message || "Request failed");
    } finally {
      setBusy(false);
    }
  }

  async function handleDownload() {
    setError("");
    if (!file) return setError("Pick a .java file first.");

    setBusy(true);
    try {
      const { blob, filename } = await downloadUpdated(file);
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch (e) {
      setError(e?.message || "Download failed");
    } finally {
      setBusy(false);
    }
  }

  function copyUpdated() {
    if (!data?.updatedSource) return;
    navigator.clipboard.writeText(data.updatedSource);
  }

  return (
    <div className="page">
      <div className="bgGlow" />

      <header className="hero">
        <div className="heroLeft">
          <div className="badge">
            <IconSparkle />
            <span>AI Code Review • Java</span>
          </div>

          <h1 className="title">
            Turn “meh” Java into <span className="accent">documented</span> Java.
          </h1>

          <p className="subtitle">
            Upload a file → get method-level review comments → auto-insert clean
            Javadocs → download updated code.
          </p>

          <div className="controls">
            <label className="filePick">
              <input
                type="file"
                accept=".java"
                onChange={(e) => setFile(e.target.files?.[0] || null)}
              />
              <span className="filePickText">Choose .java</span>
              <span className="fileName">{filename}</span>
            </label>

            <div className="btnRow">
              <button className="btnPrimary" onClick={handleReviewApply} disabled={busy}>
                {busy ? "Reviewing..." : "Review + Apply"}
              </button>
              <button className="btnGhost" onClick={handleDownload} disabled={busy}>
                Download
              </button>
            </div>
          </div>

          {error ? <div className="error">{error}</div> : null}

          {data?.result?.className ? (
            <div className="meta">
              <span className="metaItem">
                <b>Class:</b> {data.result.className}
              </span>
              <span className="metaItem">
                <b>Status:</b> {data.rawResponse}
              </span>
            </div>
          ) : null}
        </div>

        <div className="heroRight">
          <div className="mock">
            <div className="mockTop">
              <span className="dot red" />
              <span className="dot yellow" />
              <span className="dot green" />
              <span className="mockTitle">updated_source.java</span>
            </div>
            <div className="mockBody">
              <div className="line w80" />
              <div className="line w70" />
              <div className="line w90" />
              <div className="line w60" />
              <div className="line w85" />
              <div className="line w50" />
            </div>
          </div>
        </div>
      </header>

      {data ? (
        <div className="tabs">
          <button className={tab === "review" ? "tab active" : "tab"} onClick={() => setTab("review")}>
            Review
          </button>
          <button className={tab === "updated" ? "tab active" : "tab"} onClick={() => setTab("updated")}>
            Updated Source
          </button>
        </div>
      ) : null}

      {data && tab === "review" ? (
        <section className="grid">
          {(data.result?.methods || []).map((m, idx) => (
            <MethodCard key={idx} m={m} index={idx} />
          ))}
        </section>
      ) : null}

      {data && tab === "updated" ? (
        <section className="updated">
          <div className="updatedHeader">
            <div className="label">Updated Java</div>
            <button className="btnMini" onClick={copyUpdated} disabled={!data?.updatedSource}>
              Copy
            </button>
          </div>
          <CodeBlock text={data.updatedSource || ""} />
        </section>
      ) : null}

      <footer className="footer">
        <span>Spring Boot backend • Groq model • JavaParser AST edits</span>
        <span className="footerRight">Made for demos + recruiters 👀</span>
      </footer>
    </div>
  );
}
