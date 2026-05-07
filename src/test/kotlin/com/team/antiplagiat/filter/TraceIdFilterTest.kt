package com.team.antiplagiat.filter

import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class TraceIdFilterTest {

    @Test
    fun `filter generates traceId when header missing and sets response header`() {
        val filter = TraceIdFilter()

        val req = MockHttpServletRequest()
        val resp = MockHttpServletResponse()

        var chainCalled = false
        var traceIdDuringChain: String? = null

        val chain = object : FilterChain {
            override fun doFilter(request: jakarta.servlet.ServletRequest?, response: jakarta.servlet.ServletResponse?) {
                traceIdDuringChain = MDC.get(TraceIdFilter.TRACE_ID_KEY)
                chainCalled = true
            }
        }

        filter.doFilter(req, resp, chain)

        assertTrue(chainCalled, "filter must call the chain")
        assertNotNull(traceIdDuringChain, "MDC should contain traceId during request processing")
        assertEquals(16, traceIdDuringChain!!.length)

        val header = resp.getHeader(TraceIdFilter.TRACE_ID_HEADER)
        assertNotNull(header, "response must contain X-Trace-Id header")
        assertEquals(traceIdDuringChain, header)
        assertNull(MDC.get(TraceIdFilter.TRACE_ID_KEY), "MDC must be cleared after filter")
    }

    @Test
    fun `filter uses provided header and preserves it during processing`() {
        val filter = TraceIdFilter()

        val req = MockHttpServletRequest()
        val resp = MockHttpServletResponse()
        val provided = "custom-trace-12345"
        req.addHeader(TraceIdFilter.TRACE_ID_HEADER, provided)

        var chainCalled = false
        var traceIdDuringChain: String? = null

        val chain = object : FilterChain {
            override fun doFilter(request: jakarta.servlet.ServletRequest?, response: jakarta.servlet.ServletResponse?) {
                traceIdDuringChain = MDC.get(TraceIdFilter.TRACE_ID_KEY)
                chainCalled = true
            }
        }

        filter.doFilter(req, resp, chain)

        assertTrue(chainCalled)
        assertEquals(provided, traceIdDuringChain)
        assertEquals(provided, resp.getHeader(TraceIdFilter.TRACE_ID_HEADER))
        assertNull(MDC.get(TraceIdFilter.TRACE_ID_KEY))
    }
}

